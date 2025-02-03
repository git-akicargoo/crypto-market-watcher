package com.example.boot_redis_kafka_mysql.crypto.exchange.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.boot_redis_kafka_mysql.crypto.exchange.connection.WebSocketConnectionManager;
import com.example.boot_redis_kafka_mysql.crypto.exchange.model.TickerData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BinanceWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final WebSocketConnectionManager connectionManager;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.debug("Binance WebSocket Connected");
        
        String subscribeMessage = "{\"method\":\"SUBSCRIBE\",\"params\":[\"btcusdt@ticker\"],\"id\":1}";
        session.sendMessage(new TextMessage(subscribeMessage));
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            
            if (node.has("result") || node.has("id")) {
                return;
            }
            
            if (node.has("e") && "24hrTicker".equals(node.get("e").asText())) {
                TickerData tickerData = TickerData.builder()
                    .exchange("BINANCE")
                    .symbol(node.get("s").asText())
                    .price(Double.parseDouble(node.get("c").asText()))
                    .volume(Double.parseDouble(node.get("v").asText()))
                    .timestamp(node.get("E").asLong())
                    .build();
                    
                connectionManager.updateBinanceTicker(tickerData);
            }
        } catch (Exception e) {
            log.error("Failed to process Binance message", e);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Binance WebSocket transport error: {}", exception.getMessage());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("Binance WebSocket connection closed: {}", status);
    }
} 