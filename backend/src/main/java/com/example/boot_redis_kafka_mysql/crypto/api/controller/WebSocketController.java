package com.example.boot_redis_kafka_mysql.crypto.api.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.example.boot_redis_kafka_mysql.crypto.api.dto.request.SubscribeRequest;
import com.example.boot_redis_kafka_mysql.crypto.api.service.CryptoDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    
    private final CryptoDataService cryptoDataService;
    
    @MessageMapping("/crypto/subscribe")
    public void subscribe(SubscribeRequest request) {
        log.info("Received subscription request: {}", request);
        cryptoDataService.subscribe(request);
    }
} 