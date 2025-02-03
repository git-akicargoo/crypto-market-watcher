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
    private WebSocketSession session;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        log.debug("Upbit WebSocket Connected");
    }
    
    public void subscribe(String market, String type) {
        try {
            String subscribeMessage = String.format(
                "[{\"ticket\":\"UNIQUE_TICKET\"},{\"type\":\"%s\",\"codes\":[\"%s\"]}]",
                type, market
            );
            session.sendMessage(new TextMessage(subscribeMessage));
            log.info("Subscribed to Upbit market: {}, type: {}", market, type);
        } catch (Exception e) {
            log.error("Failed to subscribe to market: {}, type: {}", market, type, e);
        }
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
            log.debug("Updated Upbit ticker: {}", tickerData);
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