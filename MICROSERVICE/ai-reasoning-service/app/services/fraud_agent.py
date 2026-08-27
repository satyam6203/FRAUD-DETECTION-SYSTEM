from langgraph.graph import StateGraph, END
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain.schema import HumanMessage
from app.models.schemas import FraudAnalysisRequest, FraudAnalysisResponse, RiskLevel
from app.config.settings import settings
from typing import TypedDict, List
import json
import logging

logger = logging.getLogger(__name__)

class AgentState(TypedDict):
    payment_id: str
    amount: float
    risk_score: float
    risk_level: str
    risk_reasons: List[str]
    sender_id: str
    receiver_id: str
    ip_address: str
    device_id: str
    currency: str
    needs_escalation: bool
    analysis_complete: bool
    decision: str
    explanation: str
    recommended_action: str
    confidence: float
    reasoning_steps: List[str]

class FraudAgent:

    def __init__(self):
        self.llm = ChatGoogleGenerativeAI(
            model=settings.gemini_model,
            google_api_key=settings.gemini_api_key,
            temperature=0.1,
            convert_system_message_to_human=True
        )
        self.graph = self._build_graph()

    def _assess_risk_node(self, state: AgentState) -> AgentState:
        logger.info(f"Agent: assessing risk for payment {state['payment_id']}")
        steps = state.get("reasoning_steps", [])
        steps.append(f"Initial risk assessment: score={state['risk_score']}, "
                     f"level={state['risk_level']}")

        needs_escalation = (
                state["risk_score"] >= 70 or
                state["risk_level"] in ["HIGH", "CRITICAL"]
        )

        if needs_escalation:
            steps.append("Escalating to deep analysis — high risk detected")
        else:
            steps.append("Proceeding with standard analysis")

        return {**state,
                "needs_escalation": needs_escalation,
                "reasoning_steps": steps}

    def _deep_analysis_node(self, state: AgentState) -> AgentState:
        logger.info(f"Agent: deep analysis for payment {state['payment_id']}")
        steps = state.get("reasoning_steps", [])

        prompt = f"""You are a senior fraud investigator performing deep analysis.
        
        Transaction Details:
        - Amount: ${state['amount']} {state['currency']}
        - Risk Score: {state['risk_score']}/100
        - Risk Factors: {', '.join(state['risk_reasons'])}
        - IP: {state['ip_address']}
        - Device: {state['device_id']}
        
        This is a HIGH/CRITICAL risk transaction requiring deep investigation.
        Respond ONLY with raw JSON, no markdown, no code blocks:
        {{
            "decision": "REJECT or REVIEW",
            "explanation": "detailed analysis",
            "recommended_action": "specific action",
            "confidence": 0.95,
            "additional_steps": ["step1", "step2"]
        }}"""

        try:
            response = self.llm.invoke([HumanMessage(content=prompt)])
            content = response.content.strip()
            # Strip markdown code blocks if present
            if content.startswith("```"):
                content = content.split("```")[1]
                if content.startswith("json"):
                    content = content[4:]
            content = content.strip()
            result = json.loads(content)
            steps.append(f"Deep analysis decision: {result.get('decision')}")
            steps.extend(result.get("additional_steps", []))

            return {
                **state,
                "decision": result.get("decision", "REVIEW"),
                "explanation": result.get("explanation", ""),
                "recommended_action": result.get("recommended_action", ""),
                "confidence": result.get("confidence", 0.8),
                "reasoning_steps": steps,
                "analysis_complete": True
            }
        except Exception as e:
            logger.error(f"Deep analysis failed: {str(e)}")
            steps.append(f"Deep analysis error: {str(e)}")
            return {**state,
                    "decision": "REVIEW",
                    "explanation": f"Deep analysis error: {str(e)}",
                    "recommended_action": "Manual review required",
                    "confidence": 0.0,
                    "reasoning_steps": steps,
                    "analysis_complete": True}

    def _standard_analysis_node(self, state: AgentState) -> AgentState:
        logger.info(f"Agent: standard analysis for payment {state['payment_id']}")
        steps = state.get("reasoning_steps", [])

        prompt = f"""Analyze this low/medium risk payment transaction:
        
        - Amount: ${state['amount']} {state['currency']}
        - Risk Score: {state['risk_score']}/100
        - Risk Level: {state['risk_level']}
        - Risk Factors: {', '.join(state['risk_reasons']) if state['risk_reasons'] else 'None'}
        
        Respond ONLY with raw JSON, no markdown, no code blocks:
        {{
            "decision": "APPROVE or REVIEW",
            "explanation": "brief analysis",
            "recommended_action": "action to take",
            "confidence": 0.9
        }}"""

        try:
            response = self.llm.invoke([HumanMessage(content=prompt)])
            content = response.content.strip()
            # Strip markdown code blocks if present
            if content.startswith("```"):
                content = content.split("```")[1]
                if content.startswith("json"):
                    content = content[4:]
            content = content.strip()
            result = json.loads(content)
            steps.append(f"Standard analysis decision: {result.get('decision')}")

            return {
                **state,
                "decision": result.get("decision", "APPROVE"),
                "explanation": result.get("explanation", ""),
                "recommended_action": result.get("recommended_action", ""),
                "confidence": result.get("confidence", 0.9),
                "reasoning_steps": steps,
                "analysis_complete": True
            }
        except Exception as e:
            logger.error(f"Standard analysis failed: {str(e)}")
            steps.append(f"Standard analysis error: {str(e)}")
            return {**state,
                    "decision": "APPROVE",
                    "explanation": "Low risk transaction approved",
                    "recommended_action": "Process payment normally",
                    "confidence": 0.7,
                    "reasoning_steps": steps,
                    "analysis_complete": True}

    def _should_escalate(self, state: AgentState) -> str:
        return "deep_analysis" if state.get("needs_escalation") else "standard_analysis"

    def _build_graph(self) -> StateGraph:
        workflow = StateGraph(AgentState)

        workflow.add_node("assess_risk", self._assess_risk_node)
        workflow.add_node("deep_analysis", self._deep_analysis_node)
        workflow.add_node("standard_analysis", self._standard_analysis_node)

        workflow.set_entry_point("assess_risk")

        workflow.add_conditional_edges(
            "assess_risk",
            self._should_escalate,
            {
                "deep_analysis": "deep_analysis",
                "standard_analysis": "standard_analysis"
            }
        )

        workflow.add_edge("deep_analysis", END)
        workflow.add_edge("standard_analysis", END)

        return workflow.compile()

    def analyze(self, request: FraudAnalysisRequest) -> FraudAnalysisResponse:
        logger.info(f"Starting agent analysis for payment: {request.payment_id}")

        initial_state = AgentState(
            payment_id=request.payment_id,
            amount=request.amount,
            risk_score=request.risk_score,
            risk_level=request.risk_level,
            risk_reasons=request.risk_reasons,
            sender_id=request.sender_id,
            receiver_id=request.receiver_id,
            ip_address=request.ip_address,
            device_id=request.device_id,
            currency=request.currency,
            needs_escalation=False,
            analysis_complete=False,
            decision="",
            explanation="",
            recommended_action="",
            confidence=0.0,
            reasoning_steps=[]
        )

        final_state = self.graph.invoke(initial_state)

        return FraudAnalysisResponse(
            payment_id=request.payment_id,
            risk_level=request.risk_level,
            risk_score=request.risk_score,
            decision=final_state["decision"],
            explanation=final_state["explanation"],
            recommended_action=final_state["recommended_action"],
            confidence=final_state["confidence"],
            reasoning_steps=final_state["reasoning_steps"]
        )