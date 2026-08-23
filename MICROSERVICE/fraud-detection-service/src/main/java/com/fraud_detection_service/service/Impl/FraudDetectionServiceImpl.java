package com.fraud_detection_service.service.Impl;

import com.fraud_detection_service.Client.PaymentServiceClient;
import com.fraud_detection_service.DTO.Event.PaymentEvent;
import com.fraud_detection_service.DTO.Event.RiskAssessment;
import com.fraud_detection_service.DTO.Response.FraudAlertResponse;
import com.fraud_detection_service.enums.AlertStatus;
import com.fraud_detection_service.enums.RiskLevel;
import com.fraud_detection_service.model.FraudAlert;
import com.fraud_detection_service.repository.FraudAlertRepository;
import com.fraud_detection_service.service.FraudDetectionService;
import com.fraud_detection_service.service.RiskScoringEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final FraudAlertRepository fraudAlertRepository;
    private final RiskScoringEngine riskScoringEngine;
    private final PaymentServiceClient paymentServiceClient;

    @Override
    @Transactional
    public void processPaymentEvent(PaymentEvent event) {

        log.info("Processing payment event: {}", event.getPaymentId());

        RiskAssessment assessment = riskScoringEngine.assess(event);
        log.info("Risk assessment completed | paymentId: {} | score: {} | level: {}", event.getPaymentId(), assessment.getRiskScore(),
                assessment.getRiskLevel()
        );
        FraudAlert alert = FraudAlert.builder()
                .paymentId(event.getPaymentId())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .ipAddress(event.getIpAddress())
                .deviceId(event.getDeviceId())
                .riskReasons(assessment.getRiskReasons().isEmpty() ? "No risk factors detected"
                                : String.join("; ", assessment.getRiskReasons())
                )
                .aiExplanation(assessment.getRiskReasons().isEmpty() ? "No risk factors detected"
                                : "Rule-based analysis identified " + assessment.getRiskReasons().size() + " risk factor(s)"
                )
                .build();
        FraudAlert savedAlert = fraudAlertRepository.save(alert);

        log.info("Fraud alert saved | alertId: {} | paymentId: {} | riskLevel: {}",
                savedAlert.getId(),
                savedAlert.getPaymentId(),
                savedAlert.getRiskLevel()
        );

        if (assessment.getRiskLevel() == RiskLevel.CRITICAL || assessment.getRiskLevel() == RiskLevel.HIGH) {

            log.warn("High/Critical risk detected | paymentId: {} | action: FLAGGED", event.getPaymentId());
            paymentServiceClient.updatePaymentStatus(event.getPaymentId(), "FLAGGED");

        } else {
            log.info("Low/Medium risk detected | paymentId: {} | action: COMPLETED", event.getPaymentId());
            paymentServiceClient.updatePaymentStatus(event.getPaymentId(), "COMPLETED");
        }
    }

    @Override
    public FraudAlertResponse getAlertByPaymentId(String paymentId) {
        FraudAlert alert = fraudAlertRepository.findByPaymentId(paymentId).orElseThrow(() ->
                                new RuntimeException("Fraud alert not found for payment: " + paymentId)
                        );

        return mapToResponseDTO(alert);
    }

    @Override
    public List<FraudAlertResponse> getAlertsByStatus(AlertStatus status) {
        return fraudAlertRepository
                .findByStatus(status)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<FraudAlertResponse> getAllAlerts() {
        return fraudAlertRepository
                .findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<FraudAlertResponse> getAlertsByRiskLevel(RiskLevel riskLevel) {
        return fraudAlertRepository
                .findByRiskLevel(riskLevel)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private FraudAlertResponse mapToResponseDTO(FraudAlert alert) {
        return FraudAlertResponse.builder()
                .id(alert.getId())
                .paymentId(alert.getPaymentId())
                .senderId(alert.getSenderId())
                .receiverId(alert.getReceiverId())
                .amount(alert.getAmount())
                .currency(alert.getCurrency())
                .riskScore(alert.getRiskScore())
                .riskLevel(alert.getRiskLevel())
                .status(alert.getStatus())
                .riskReasons(alert.getRiskReasons())
                .aiExplanation(alert.getAiExplanation())
                .createdAt(alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null)
                .build();
    }
}
