package com.example.boot_redis_kafka_mysql.crypto.exchange.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.boot_redis_kafka_mysql.crypto.exchange.config.ExchangeProperties;
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
    private final ExchangeProperties properties;
    private WebSocketSession session;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        log.debug("Binance WebSocket Connected");
    }
    
    public void subscribe(String symbol, String type) {
        try {
            // BTC-USDT -> btcusdt 형식으로 변환
            String formattedSymbol = symbol.toLowerCase().replace("-", "");
            String subscribeMessage = String.format(
                "{\"method\":\"SUBSCRIBE\",\"params\":[\"%s@%s\"],\"id\":%d}",
                formattedSymbol, type, System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(subscribeMessage));
            log.info("Subscribed to Binance symbol: {}, type: {}", symbol, type);
        } catch (Exception e) {
            log.error("Failed to subscribe to symbol: {}, type: {}", symbol, type, e);
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            
            if (node.has("e") && "24hrTicker".equals(node.get("e").asText())) {
                TickerData tickerData = TickerData.builder()
                    .exchange("BINANCE")
                    .symbol(node.get("s").asText())
                    .price(Double.parseDouble(node.get("c").asText()))
                    .volume(Double.parseDouble(node.get("v").asText()))
                    .timestamp(node.get("E").asLong())
                    .build();
                    
                connectionManager.updateBinanceTicker(tickerData);
                log.debug("Updated Binance ticker: {}", tickerData);
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
        this.session = null;
        
        try {
            log.info("Attempting immediate reconnection to Binance...");
            WebSocketClient client = new StandardWebSocketClient();
            client.execute(this, properties.getBinance().getWsUrl());
        } catch (Exception e) {
            log.error("Failed to reconnect to Binance", e);
        }
    }
    
    public boolean isConnected() {
        return session != null && session.isOpen();
    }
} 