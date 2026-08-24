package com.fraud_detection_service.service;

import com.fraud_detection_service.model.FraudAlert;

public interface NotificationService {

    void sendFraudAlert(FraudAlert alert);
}
