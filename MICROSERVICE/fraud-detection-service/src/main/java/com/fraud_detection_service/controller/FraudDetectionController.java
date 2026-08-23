package com.fraud_detection_service.controller;

import com.fraud_detection_service.DTO.Response.FraudAlertResponse;
import com.fraud_detection_service.enums.AlertStatus;
import com.fraud_detection_service.enums.RiskLevel;
import com.fraud_detection_service.service.FraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fraud Detection API", description = "Endpoints for fraud alerts and risk scores")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    @GetMapping("/alerts")
    @Operation(summary = "Get all fraud alerts")
    public ResponseEntity<List<FraudAlertResponse>> getAllAlerts() {
        return ResponseEntity.ok(fraudDetectionService.getAllAlerts());
    }

    @GetMapping("/alerts/payment/{paymentId}")
    @Operation(summary = "Get fraud alert by payment ID")
    public ResponseEntity<FraudAlertResponse> getAlertByPaymentId(
            @PathVariable String paymentId) {
        return ResponseEntity.ok(fraudDetectionService.getAlertByPaymentId(paymentId));
    }

    @GetMapping("/alerts/status/{status}")
    @Operation(summary = "Get fraud alerts by status")
    public ResponseEntity<List<FraudAlertResponse>> getAlertsByStatus(
            @PathVariable AlertStatus status) {
        return ResponseEntity.ok(fraudDetectionService.getAlertsByStatus(status));
    }

    @GetMapping("/alerts/risk/{riskLevel}")
    @Operation(summary = "Get fraud alerts by risk level")
    public ResponseEntity<List<FraudAlertResponse>> getAlertsByRiskLevel(
            @PathVariable RiskLevel riskLevel) {
        return ResponseEntity.ok(fraudDetectionService.getAlertsByRiskLevel(riskLevel));
    }
}
