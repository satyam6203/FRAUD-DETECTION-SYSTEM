from pydantic import BaseModel
from typing import List, Optional
from enum import Enum

class RiskLevel(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"

class FraudAnalysisRequest(BaseModel):
    payment_id: str
    sender_id: str
    receiver_id: str
    amount: float
    currency: str
    ip_address: str
    device_id: str
    risk_score: float
    risk_level: RiskLevel
    risk_reasons: List[str]

class FraudAnalysisResponse(BaseModel):
    payment_id: str
    risk_level: RiskLevel
    risk_score: float
    decision: str
    explanation: str
    recommended_action: str
    confidence: float
    reasoning_steps: List[str]

class AgentState(BaseModel):
    payment_id: str
    amount: float
    risk_score: float
    risk_level: str
    risk_reasons: List[str]
    needs_more_info: bool = False
    analysis_complete: bool = False
    decision: str = ""
    explanation: str = ""
    recommended_action: str = ""
    confidence: float = 0.0
    reasoning_steps: List[str] = []