package com.fraud_detection_service.service;

import com.fraud_detection_service.DTO.Event.PaymentEvent;
import com.fraud_detection_service.DTO.Response.FraudAlertResponse;
import com.fraud_detection_service.enums.AlertStatus;
import com.fraud_detection_service.enums.RiskLevel;

import java.util.List;

public interface FraudDetectionService {

    void processPaymentEvent(PaymentEvent event);

    FraudAlertResponse getAlertByPaymentId(String paymentId);

    List<FraudAlertResponse> getAlertsByStatus(AlertStatus status);

    List<FraudAlertResponse> getAllAlerts();

    List<FraudAlertResponse> getAlertsByRiskLevel(RiskLevel riskLevel);
}
