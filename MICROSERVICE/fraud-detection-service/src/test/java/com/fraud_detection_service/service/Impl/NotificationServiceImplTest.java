package com.fraud_detection_service.service.Impl;

import com.fraud_detection_service.enums.RiskLevel;
import com.fraud_detection_service.model.FraudAlert;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceImplTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private static final String ANALYST = "analyst@bank.com";

    @Test
    void sendsCriticalAlertWithCorrectSubjectAndContent() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
        NotificationServiceImpl service = new NotificationServiceImpl(mailSender, ANALYST, "no-reply@bank.com");

        service.sendFraudAlert(alert(RiskLevel.CRITICAL));

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage message = captor.getValue();

        assertThat(message.getSubject())
                .isEqualTo("\uD83D\uDEA8 CRITICAL FRAUD ALERT - Payment pay-123");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(ANALYST);

        String body = bodyOf(message);
        assertThat(body)
                .contains("pay-123")
                .contains("sender-1")
                .contains("receiver-2")
                .contains("INR 15,000.00")
                .contains("85.0")
                .contains("CRITICAL")
                .contains("10.0.0.1")
                .contains("device-9")
                .contains("Amount exceeds threshold")
                .contains("Rule-based analysis")
                .contains("Block the payment immediately");
    }

    @Test
    void sendsHighAlertWithHighSubject() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
        NotificationServiceImpl service = new NotificationServiceImpl(mailSender, ANALYST, "no-reply@bank.com");

        service.sendFraudAlert(alert(RiskLevel.HIGH));

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getSubject())
                .isEqualTo("\u26A0\uFE0F HIGH RISK PAYMENT DETECTED - pay-123");
    }

    @Test
    void skipsSendingWhenNoRecipientConfigured() {
        NotificationServiceImpl service = new NotificationServiceImpl(mailSender, "  ", "no-reply@bank.com");

        service.sendFraudAlert(alert(RiskLevel.HIGH));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void doesNotPropagateSmtpFailures() {
        when(mailSender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(MimeMessage.class));
        NotificationServiceImpl service = new NotificationServiceImpl(mailSender, ANALYST, "no-reply@bank.com");

        assertThatCode(() -> service.sendFraudAlert(alert(RiskLevel.CRITICAL)))
                .doesNotThrowAnyException();
    }

    private FraudAlert alert(RiskLevel level) {
        double score = level == RiskLevel.CRITICAL ? 85.0 : 55.0;
        return FraudAlert.builder()
                .paymentId("pay-123")
                .senderId("sender-1")
                .receiverId("receiver-2")
                .amount(new BigDecimal("15000"))
                .currency("INR")
                .riskScore(score)
                .riskLevel(level)
                .ipAddress("10.0.0.1")
                .deviceId("device-9")
                .riskReasons("Amount exceeds threshold; Suspicious IP range")
                .aiExplanation("Rule-based analysis identified 2 risk factor(s)")
                .createdAt(Instant.parse("2026-08-23T10:15:30Z"))
                .build();
    }

    private String bodyOf(MimeMessage message) throws Exception {
        return extractText(message.getContent());
    }

    private String extractText(Object content) throws Exception {
        if (content instanceof Multipart multipart) {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                text.append(extractText(multipart.getBodyPart(i).getContent()));
            }
            return text.toString();
        }
        return String.valueOf(content);
    }
}
