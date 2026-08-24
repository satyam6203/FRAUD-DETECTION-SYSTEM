package com.payment_service.Controller;

import com.payment_service.DTO.Request.PaymentRequest;
import com.payment_service.DTO.Request.PaymentResponse;
import com.payment_service.Enums.PaymentStatus;
import com.payment_service.Service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment API", description = "Endpoints for managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Initiate a new payment",
            description = "Creates a payment and publishes it to Kafka for fraud analysis")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request from sender: {}", request.getSenderId());
        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/sender/{senderId}")
    @Operation(summary = "Get all payments by sender ID")
    public ResponseEntity<List<PaymentResponse>> getPaymentsBySender(
            @PathVariable String senderId) {
        return ResponseEntity.ok(paymentService.getPaymentsBySender(senderId));
    }

    @PatchMapping("/{paymentId}/status")
    @Operation(summary = "Update payment status",
            description = "Used by fraud detection service to update payment status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }
}
