package com.payment_service.DTO.Request;

import com.payment_service.Enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String id;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String ipAddress;
    private String deviceId;
    private String createdAt;
    private String updatedAt;
    private String message;
}
