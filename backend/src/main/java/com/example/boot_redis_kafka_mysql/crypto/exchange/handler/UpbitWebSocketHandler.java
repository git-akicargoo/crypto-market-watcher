package com.example.boot_redis_kafka_mysql.crypto.exchange.handler;

import java.nio.charset.StandardCharsets;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.example.boot_redis_kafka_mysql.crypto.exchange.connection.WebSocketConnectionManager;
import com.example.boot_redis_kafka_mysql.crypto.exchange.model.TickerData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UpbitWebSocketHandler extends AbstractWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final WebSocketConnectionManager connectionManager;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.debug("Upbit WebSocket Connected");
        
        String subscribeMessage = "[{\"ticket\":\"test\"},{\"type\":\"ticker\",\"codes\":[\"KRW-BTC\"]}]";
        session.sendMessage(new TextMessage(subscribeMessage));
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            handleMessage(message.getPayload());
        } catch (Exception e) {
            log.error("Failed to process Upbit text message", e);
        }
    }
    
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            String payload = new String(message.getPayload().array(), StandardCharsets.UTF_8);
            handleMessage(payload);
        } catch (Exception e) {
            log.error("Failed to process Upbit binary message", e);
        }
    }
    
    private void handleMessage(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        
        if (node.has("type") && "ticker".equals(node.get("type").asText())) {
            TickerData tickerData = TickerData.builder()
                .exchange("UPBIT")
                .symbol(node.get("code").asText())
                .price(node.get("trade_price").asDouble())
                .volume(node.get("trade_volume").asDouble())
                .timestamp(node.get("timestamp").asLong())
                .build();
                
            connectionManager.updateUpbitTicker(tickerData);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Upbit WebSocket transport error: {}", exception.getMessage());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("Upbit WebSocket connection closed: {}", status);
    }
} 