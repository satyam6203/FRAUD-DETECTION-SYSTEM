package com.fraud_detection_service.repository;

import com.fraud_detection_service.enums.AlertStatus;
import com.fraud_detection_service.enums.RiskLevel;
import com.fraud_detection_service.model.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, String> {

    Optional<FraudAlert> findByPaymentId(String paymentId);

    List<FraudAlert> findByStatus(AlertStatus status);

    List<FraudAlert> findByRiskLevel(RiskLevel riskLevel);

    List<FraudAlert> findBySenderId(String senderId);

    @Query("SELECT COUNT(f) FROM FraudAlert f WHERE f.senderId = :senderId " +
            "AND f.createdAt >= :since AND f.riskLevel IN ('HIGH', 'CRITICAL')")
    long countHighRiskAlertsBySender(@Param("senderId") String senderId,
                                     @Param("since") Instant since);
}
