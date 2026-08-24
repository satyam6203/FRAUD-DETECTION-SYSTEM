package com.fraud_detection_service.service.Impl;

import com.fraud_detection_service.enums.RiskLevel;
import com.fraud_detection_service.model.FraudAlert;
import com.fraud_detection_service.service.NotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss 'UTC'").withZone(ZoneId.of("UTC"));

    private final JavaMailSender mailSender;
    private final String recipient;
    private final String from;

    @Autowired(required = false)
    public NotificationServiceImpl(JavaMailSender mailSender,
                                   @Value("${fraud.alert.recipient:}") String recipient,
                                   @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.recipient = recipient;
        this.from = from;
    }

    @Override
    public void sendFraudAlert(FraudAlert alert) {

        if (recipient == null || recipient.isBlank()) {
            log.warn("No fraud analyst email configured (FRAUD_ALERT_EMAIL) | skipping email alert | paymentId: {}",
                    alert.getPaymentId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipient.split(","));
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setSubject(buildSubject(alert));
            helper.setText(buildEmailBody(alert), true);

            mailSender.send(message);

            log.info("Fraud alert email sent | paymentId: {} | riskLevel: {} | to: {}",
                    alert.getPaymentId(), alert.getRiskLevel(), recipient);
        } catch (Exception e) {
            log.error("Failed to send fraud alert email | paymentId: {} | reason: {}",
                    alert.getPaymentId(), e.getMessage(), e);
        }
    }

    private String buildSubject(FraudAlert alert) {
        return alert.getRiskLevel() == RiskLevel.CRITICAL
                ? "\uD83D\uDEA8 CRITICAL FRAUD ALERT - Payment " + alert.getPaymentId()
                : "\u26A0\uFE0F HIGH RISK PAYMENT DETECTED - " + alert.getPaymentId();
    }

    private String buildRecommendedAction(FraudAlert alert) {
        return alert.getRiskLevel() == RiskLevel.CRITICAL
                ? "Block the payment immediately, freeze the sender account and escalate to the compliance team."
                : "Place the payment on hold and review manually before releasing the funds.";
    }

    private String buildEmailBody(FraudAlert alert) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2 style="color: %s;">%s</h2>
                    <p>A %s-risk transaction was detected by the fraud detection engine.</p>
                    <table cellpadding="8" cellspacing="0" border="1" style="border-collapse: collapse; border-color: #ddd;">
                        <tr><td><b>Payment ID</b></td><td>%s</td></tr>
                        <tr><td><b>Sender ID</b></td><td>%s</td></tr>
                        <tr><td><b>Receiver ID</b></td><td>%s</td></tr>
                        <tr><td><b>Transaction Amount</b></td><td>%s</td></tr>
                        <tr><td><b>Risk Score</b></td><td>%s / 100</td></tr>
                        <tr><td><b>Risk Level</b></td><td>%s</td></tr>
                        <tr><td><b>IP Address</b></td><td>%s</td></tr>
                        <tr><td><b>Device ID</b></td><td>%s</td></tr>
                        <tr><td><b>Fraud Risk Reasons</b></td><td>%s</td></tr>
                        <tr><td><b>AI Explanation</b></td><td>%s</td></tr>
                        <tr><td><b>Recommended Action</b></td><td>%s</td></tr>
                        <tr><td><b>Detected At (UTC)</b></td><td>%s</td></tr>
                    </table>
                    <p style="color: #888; font-size: 12px;">This is an automated message from the Fraud Detection Service. Do not reply.</p>
                </body>
                </html>
                """.formatted(
                headerColor(alert),
                buildSubject(alert),
                alert.getRiskLevel().name().toLowerCase(),
                escape(alert.getPaymentId()),
                escape(alert.getSenderId()),
                escape(alert.getReceiverId()),
                escape(formatAmount(alert)),
                escape(String.valueOf(alert.getRiskScore())),
                escape(alert.getRiskLevel().name()),
                escape(alert.getIpAddress()),
                escape(alert.getDeviceId()),
                escape(alert.getRiskReasons() == null ? "N/A" : alert.getRiskReasons().replace("; ", "<br/>")),
                escape(alert.getAiExplanation() == null ? "N/A" : alert.getAiExplanation()),
                escape(buildRecommendedAction(alert)),
                alert.getCreatedAt() != null ? TIMESTAMP_FORMAT.format(alert.getCreatedAt()) : "N/A"
        );
    }

    private String headerColor(FraudAlert alert) {
        return alert.getRiskLevel() == RiskLevel.CRITICAL ? "#c0392b" : "#e67e22";
    }

    private String formatAmount(FraudAlert alert) {
        return alert.getCurrency() + " " + String.format(Locale.US, "%,.2f", alert.getAmount());
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
