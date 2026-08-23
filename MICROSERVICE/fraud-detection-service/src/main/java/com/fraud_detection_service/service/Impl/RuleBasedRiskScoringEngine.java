package com.fraud_detection_service.service.Impl;

import com.fraud_detection_service.DTO.Event.PaymentEvent;
import com.fraud_detection_service.DTO.Event.RiskAssessment;
import com.fraud_detection_service.enums.RiskLevel;
import com.fraud_detection_service.service.RiskScoringEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RuleBasedRiskScoringEngine implements RiskScoringEngine {

    @Value("${fraud.rules.high-amount-threshold}")
    private double highAmountThreshold;

    @Value("${fraud.rules.suspicious-amount-threshold}")
    private double suspiciousAmountThreshold;

    private static final List<String> SUSPICIOUS_IP_PREFIXES =
            List.of("45.33", "192.241", "198.199", "104.236");

    @Override
    public RiskAssessment assess(PaymentEvent event) {
        log.info("Assessing risk for payment: {}", event.getPaymentId());
        List<String> riskReasons = new ArrayList<>();
        double riskScore = 0.0;

        if (event.getAmount() != null && event.getAmount().compareTo(BigDecimal.valueOf(suspiciousAmountThreshold)) >= 0) {
            riskScore += 40.0;
            riskReasons.add("Transaction amount exceeds suspicious threshold of $" + suspiciousAmountThreshold);

        } else if (event.getAmount() != null && event.getAmount().compareTo(BigDecimal.valueOf(highAmountThreshold)) >= 0) {
            riskScore += 20.0;
            riskReasons.add("Transaction amount exceeds high threshold of $" + highAmountThreshold);
        }

        if (event.getIpAddress() != null) {
            boolean suspiciousIp = SUSPICIOUS_IP_PREFIXES.stream().anyMatch(prefix -> event.getIpAddress().startsWith(prefix));

            if (suspiciousIp) {
                riskScore += 30.0;
                riskReasons.add("Transaction originated from suspicious IP: " + event.getIpAddress());
            }
        }

        if (event.getDeviceId() != null && event.getDeviceId().toLowerCase().contains("unknown")) {
            riskScore += 20.0;
            riskReasons.add("Transaction from unknown device: " + event.getDeviceId());
        }

        if (event.getSenderId() != null && event.getSenderId().equals(event.getReceiverId())) {
            riskScore += 50.0;
            riskReasons.add("Sender and receiver are the same account");
        }
        riskScore = Math.min(riskScore, 100.0);
        RiskLevel riskLevel = determineRiskLevel(riskScore);
        boolean requiresAiAnalysis = riskScore >= 40.0;
        log.info("Risk assessment complete | payment: {} | score: {} | level: {}", event.getPaymentId(), riskScore, riskLevel);

        return RiskAssessment.builder()
                .paymentId(event.getPaymentId())
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .riskReasons(riskReasons)
                .requiresAiAnalysis(requiresAiAnalysis)
                .build();
    }

    private RiskLevel determineRiskLevel(double score) {
        if (score >= 70.0) return RiskLevel.CRITICAL;
        if (score >= 40.0) return RiskLevel.HIGH;
        if (score >= 20.0) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}
