package com.payment_service.Service.Impl;

import com.payment_service.DTO.Event.PaymentEvent;
import com.payment_service.DTO.Request.PaymentRequest;
import com.payment_service.DTO.Request.PaymentResponse;
import com.payment_service.Enums.PaymentStatus;
import com.payment_service.Kafka.PaymentEventProducer;
import com.payment_service.Model.Payment;
import com.payment_service.Repository.PaymentRepository;
import com.payment_service.Service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {

        log.info("Initiating payment from sender: {} to receiver: {}", request.getSenderId(),
                request.getReceiverId()
        );

        Payment payment = Payment.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .ipAddress(request.getIpAddress())
                .deviceId(request.getDeviceId())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment saved successfully with id: {}", savedPayment.getId());

        PaymentEvent event = PaymentEvent.builder()
                .paymentId(savedPayment.getId())
                .senderId(savedPayment.getSenderId())
                .receiverId(savedPayment.getReceiverId())
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .ipAddress(savedPayment.getIpAddress())
                .deviceId(savedPayment.getDeviceId())
                .createdAt(savedPayment.getCreatedAt())
                .build();

        paymentEventProducer.publishPaymentEvent(event);

        return mapToResponse(savedPayment, "Payment initiated and sent for fraud analysis");
    }

    @Override
    @Cacheable(value = "payments", key = "#paymentId")
    public PaymentResponse getPaymentById(String paymentId) {

        log.info("Fetching payment by id: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found: " + paymentId)
                );
        return mapToResponse(payment, "Payment retrieved successfully");
    }

    @Override
    public List<PaymentResponse> getPaymentsBySender(String senderId) {

        log.info("Fetching payments for sender: {}", senderId);

        return paymentRepository
                .findBySenderId(senderId)
                .stream()
                .map(payment -> mapToResponse(payment, null))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "payments", key = "#paymentId")
    public PaymentResponse updatePaymentStatus(String paymentId, PaymentStatus status) {

        log.info("Updating payment {} status to {}", paymentId, status);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found: " + paymentId)
                );
        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        return mapToResponse(updatedPayment, "Payment status updated");
    }

    private PaymentResponse mapToResponse(Payment payment, String message) {

        return PaymentResponse.builder()
                .id(payment.getId())
                .senderId(payment.getSenderId())
                .receiverId(payment.getReceiverId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .ipAddress(payment.getIpAddress())
                .deviceId(payment.getDeviceId())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null)
                .updatedAt(payment.getUpdatedAt() != null ? payment.getUpdatedAt().toString() : null)
                .message(message)
                .build();
    }
}