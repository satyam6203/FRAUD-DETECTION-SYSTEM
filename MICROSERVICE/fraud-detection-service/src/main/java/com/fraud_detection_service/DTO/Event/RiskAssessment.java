package com.fraud_detection_service.DTO.Event;

import com.fraud_detection_service.enums.RiskLevel;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    private String paymentId;
    private Double riskScore;
    private RiskLevel riskLevel;
    private List<String> riskReasons;
    private boolean requiresAiAnalysis;
}
