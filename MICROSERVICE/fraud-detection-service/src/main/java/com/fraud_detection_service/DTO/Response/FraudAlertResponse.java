package com.fraud_detection_service.DTO.Response;

import com.fraud_detection_service.enums.AlertStatus;
import com.fraud_detection_service.enums.RiskLevel;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlertResponse {

    private String id;
    private String paymentId;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String currency;
    private Double riskScore;
    private RiskLevel riskLevel;
    private AlertStatus status;
    private String riskReasons;
    private String aiExplanation;
    private String createdAt;
}
