from fastapi import APIRouter, HTTPException
from app.models.schemas import FraudAnalysisRequest, FraudAnalysisResponse
from app.services.fraud_analyzer import FraudAnalyzer
from app.services.fraud_agent import FraudAgent
import logging

logger = logging.getLogger(__name__)
router = APIRouter()

analyzer = FraudAnalyzer()
agent = FraudAgent()

@router.post("/analyze", response_model=FraudAnalysisResponse)
async def analyze_fraud(request: FraudAnalysisRequest):
    """Simple LangChain analysis — single LLM call"""
    logger.info(f"Received analysis request for payment: {request.payment_id}")
    try:
        return analyzer.analyze(request)
    except Exception as e:
        logger.error(f"Analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/analyze/agent", response_model=FraudAnalysisResponse)
async def analyze_fraud_agent(request: FraudAnalysisRequest):
    """LangGraph agentic analysis — multi-step reasoning"""
    logger.info(f"Received agent analysis request for payment: {request.payment_id}")
    try:
        return agent.analyze(request)
    except Exception as e:
        logger.error(f"Agent analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/health")
async def health():
    return {"status": "UP", "service": "ai-reasoning-service"}