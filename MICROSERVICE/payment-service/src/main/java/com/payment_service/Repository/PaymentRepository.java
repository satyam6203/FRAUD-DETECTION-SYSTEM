package com.payment_service.Repository;

import com.payment_service.Enums.PaymentStatus;
import com.payment_service.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findBySenderId(String senderId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("""
            SELECT COUNT(p)
            FROM Payment p
            WHERE p.senderId = :senderId
            AND p.createdAt >= :since
            """)
    long countRecentPaymentsBySender(
            @Param("senderId") String senderId,
            @Param("since") Instant since
    );

    @Query("""
            SELECT SUM(p.amount)
            FROM Payment p
            WHERE p.senderId = :senderId
            AND p.createdAt >= :since
            """)
    BigDecimal sumRecentAmountBySender(
            @Param("senderId") String senderId,
            @Param("since") Instant since
    );
}
