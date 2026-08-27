from langchain_google_genai import ChatGoogleGenerativeAI
from langchain.prompts import ChatPromptTemplate
from langchain.schema import HumanMessage
from app.models.schemas import FraudAnalysisRequest, FraudAnalysisResponse, RiskLevel
from app.config.settings import settings
import json
import logging

logger = logging.getLogger(__name__)

class FraudAnalyzer:

    def __init__(self):
        self.llm = ChatGoogleGenerativeAI(
            model=settings.gemini_model,
            google_api_key=settings.gemini_api_key,
            temperature=0.1,
            convert_system_message_to_human=True
        )

    def analyze(self, request: FraudAnalysisRequest) -> FraudAnalysisResponse:
        logger.info(f"Analyzing fraud for payment: {request.payment_id}")

        system_prompt = """You are an expert fraud detection analyst at a financial institution.
        Your role is to analyze payment transactions and provide detailed fraud risk assessments.
        You must respond ONLY with a valid JSON object, no markdown, no explanation outside JSON.
        
        Analyze the transaction and return this exact JSON structure:
        {
            "decision": "APPROVE or REJECT or REVIEW",
            "explanation": "detailed explanation of your analysis",
            "recommended_action": "specific action to take",
            "confidence": 0.0 to 1.0,
            "reasoning_steps": ["step 1", "step 2", "step 3"]
        }"""

        user_prompt = f"""Analyze this payment transaction for fraud:
        
        Payment ID: {request.payment_id}
        Amount: ${request.amount} {request.currency}
        Sender: {request.sender_id}
        Receiver: {request.receiver_id}
        IP Address: {request.ip_address}
        Device: {request.device_id}
        Risk Score: {request.risk_score}/100
        Risk Level: {request.risk_level}
        Risk Factors Detected: {', '.join(request.risk_reasons) if request.risk_reasons else 'None'}
        
        Provide a comprehensive fraud analysis and recommendation."""

        try:
            response = self.llm.invoke([
                HumanMessage(content=system_prompt + "\n\n" + user_prompt)
            ])

            content = response.content.strip()
            # Strip markdown code blocks if present
            if content.startswith("```"):
                content = content.split("```")[1]
                if content.startswith("json"):
                    content = content[4:]
            content = content.strip()

            if not content:
                raise ValueError("Empty response from LLM")

            result = json.loads(content)
            logger.info(f"Analysis complete for payment: {request.payment_id} "
                        f"| decision: {result.get('decision')}")

            return FraudAnalysisResponse(
                payment_id=request.payment_id,
                risk_level=request.risk_level,
                risk_score=request.risk_score,
                decision=result.get("decision", "REVIEW"),
                explanation=result.get("explanation", ""),
                recommended_action=result.get("recommended_action", ""),
                confidence=result.get("confidence", 0.5),
                reasoning_steps=result.get("reasoning_steps", [])
            )

        except Exception as e:
            logger.error(f"Error analyzing payment {request.payment_id}: {str(e)}")
            return FraudAnalysisResponse(
                payment_id=request.payment_id,
                risk_level=request.risk_level,
                risk_score=request.risk_score,
                decision="REVIEW",
                explanation=f"Analysis failed: {str(e)}",
                recommended_action="Manual review required",
                confidence=0.0,
                reasoning_steps=["Analysis failed — manual review required"]
            )