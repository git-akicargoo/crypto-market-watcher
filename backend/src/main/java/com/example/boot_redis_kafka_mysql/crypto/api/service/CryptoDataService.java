package com.example.boot_redis_kafka_mysql.crypto.api.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.boot_redis_kafka_mysql.crypto.api.dto.request.SubscribeRequest;
import com.example.boot_redis_kafka_mysql.crypto.exchange.connection.WebSocketConnectionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoDataService {
    private final WebSocketConnectionManager connectionManager;
    private final SimpMessagingTemplate messagingTemplate;

    public void subscribe(SubscribeRequest request) {
        log.info("Processing subscription request: {}", request);
        
        try {
            String destination = "/topic/crypto/ticker/" + 
                request.getExchange().toLowerCase();
                
            if ("UPBIT".equals(request.getExchange())) {
                connectionManager.subscribeUpbitMarket(request.getMarket(), request.getType());
                messagingTemplate.convertAndSend(destination, 
                    connectionManager.getLastUpbitTicker());
                    
            } else if ("BINANCE".equals(request.getExchange())) {
                connectionManager.subscribeBinanceMarket(request.getMarket(), request.getType());
                messagingTemplate.convertAndSend(destination, 
                    connectionManager.getLastBinanceTicker());
            }
            
            log.info("Successfully subscribed to {}: {}", 
                request.getExchange(), request.getMarket());
                
        } catch (Exception e) {
            log.error("Failed to process subscription request: {}", request, e);
            messagingTemplate.convertAndSend("/topic/errors", 
                "Failed to subscribe: " + e.getMessage());
            throw new RuntimeException("Subscription failed", e);
        }
    }
} 