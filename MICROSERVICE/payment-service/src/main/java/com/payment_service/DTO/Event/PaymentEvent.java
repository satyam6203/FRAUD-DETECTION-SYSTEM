package com.payment_service.DTO.Event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {

    private String paymentId;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String currency;
    private String ipAddress;
    private String deviceId;
    private Instant createdAt;
}
