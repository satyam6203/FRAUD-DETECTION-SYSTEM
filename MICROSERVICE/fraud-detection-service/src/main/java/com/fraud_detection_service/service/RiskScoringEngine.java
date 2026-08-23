package com.fraud_detection_service.service;

import com.fraud_detection_service.DTO.Event.PaymentEvent;
import com.fraud_detection_service.DTO.Event.RiskAssessment;

public interface RiskScoringEngine {
    RiskAssessment assess(PaymentEvent event);
}
