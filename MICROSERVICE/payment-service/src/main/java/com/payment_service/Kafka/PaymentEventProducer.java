package com.payment_service.Kafka;

import com.payment_service.DTO.Event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${spring.kafka.topics.payment-events}")
    private String paymentEventsTopic;

    public void publishPaymentEvent(PaymentEvent event) {
        log.info("Publishing payment event for paymentId: {}", event.getPaymentId());

        CompletableFuture<SendResult<String, PaymentEvent>> future =
                kafkaTemplate.send(paymentEventsTopic, event.getPaymentId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Payment event published successfully for paymentId: {} | " +
                                "partition: {} | offset: {}",
                        event.getPaymentId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish payment event for paymentId: {} | error: {}",
                        event.getPaymentId(), ex.getMessage());
            }
        });
    }
}
