package com.fraud_detection_service.Service;

import com.fraud_detection_service.DTO.Request.PaymentRequest;
import com.fraud_detection_service.DTO.Rquest.PaymentResponse;
import com.fraud_detection_service.Enums.PaymentStatus;

import java.util.List;

public interface PaymentService {

    PaymentResponse initiatePayment(PaymentRequest request);

    PaymentResponse getPaymentById(String paymentId);

    List<PaymentResponse> getPaymentsBySender(String senderId);

    PaymentResponse updatePaymentStatus(
            String paymentId,
            PaymentStatus status
    );
}