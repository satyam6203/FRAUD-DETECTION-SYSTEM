package com.fraud_detection_service.Client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.payment-service.url}")
    private String paymentServiceUrl;

    public void updatePaymentStatus(String paymentId, String status) {
        log.info("Updating payment {} status to {}", paymentId, status);
        try {
            String response = webClientBuilder.build()
                    .patch()
                    .uri(paymentServiceUrl + "/api/v1/payments/{id}/status?status={status}",
                            paymentId, status)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Payment {} status updated to {} | response: {}", paymentId, status, response);
        } catch (Exception e) {
            log.error("Failed to update payment {} status: {}", paymentId, e.getMessage());
        }
    }
}
