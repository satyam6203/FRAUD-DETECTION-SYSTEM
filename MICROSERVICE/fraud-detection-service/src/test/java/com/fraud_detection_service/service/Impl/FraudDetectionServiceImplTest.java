package com.fraud_detection_service.service.Impl;

import com.fraud_detection_service.Client.PaymentServiceClient;
import com.fraud_detection_service.DTO.Event.PaymentEvent;
import com.fraud_detection_service.DTO.Event.RiskAssessment;
import com.fraud_detection_service.enums.RiskLevel;
import com.fraud_detection_service.model.FraudAlert;
import com.fraud_detection_service.repository.FraudAlertRepository;
import com.fraud_detection_service.service.NotificationService;
import com.fraud_detection_service.service.RiskScoringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceImplTest {

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @Mock
    private RiskScoringEngine riskScoringEngine;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FraudDetectionServiceImpl service;

    private PaymentEvent event;

    @BeforeEach
    void setUp() {
        event = PaymentEvent.builder()
                .paymentId("pay-123")
                .senderId("sender-1")
                .receiverId("receiver-2")
                .amount(new BigDecimal("15000"))
                .currency("INR")
                .ipAddress("10.0.0.1")
                .deviceId("device-9")
                .build();
    }

    @ParameterizedTest
    @EnumSource(value = RiskLevel.class, names = {"HIGH", "CRITICAL"})
    void sendsEmailAndFlagsPaymentForHighAndCritical(RiskLevel level) {
        when(riskScoringEngine.assess(any(PaymentEvent.class))).thenReturn(assessment(level));
        when(fraudAlertRepository.save(any(FraudAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.processPaymentEvent(event);

        ArgumentCaptor<FraudAlert> captor = ArgumentCaptor.forClass(FraudAlert.class);
        verify(fraudAlertRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isNotNull();

        verify(notificationService).sendFraudAlert(any(FraudAlert.class));
        verify(paymentServiceClient).updatePaymentStatus("pay-123", "FLAGGED");
    }

    @ParameterizedTest
    @EnumSource(value = RiskLevel.class, names = {"LOW", "MEDIUM"})
    void doesNotSendEmailForLowAndMedium(RiskLevel level) {
        when(riskScoringEngine.assess(any(PaymentEvent.class))).thenReturn(assessment(level));
        when(fraudAlertRepository.save(any(FraudAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.processPaymentEvent(event);

        verify(notificationService, never()).sendFraudAlert(any(FraudAlert.class));
        verify(paymentServiceClient).updatePaymentStatus("pay-123", "COMPLETED");
    }

    private RiskAssessment assessment(RiskLevel level) {
        double score = switch (level) {
            case CRITICAL -> 85.0;
            case HIGH -> 55.0;
            case MEDIUM -> 25.0;
            case LOW -> 5.0;
        };
        return RiskAssessment.builder()
                .paymentId("pay-123")
                .riskScore(score)
                .riskLevel(level)
                .riskReasons(List.of("Amount exceeds threshold"))
                .requiresAiAnalysis(level == RiskLevel.HIGH || level == RiskLevel.CRITICAL)
                .build();
    }
}
