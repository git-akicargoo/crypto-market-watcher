package com.example.boot_redis_kafka_mysql.crypto.exchange.connection;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.example.boot_redis_kafka_mysql.crypto.exchange.config.ExchangeProperties;
import com.example.boot_redis_kafka_mysql.crypto.exchange.handler.BinanceWebSocketHandler;
import com.example.boot_redis_kafka_mysql.crypto.exchange.handler.UpbitWebSocketHandler;
import com.example.boot_redis_kafka_mysql.crypto.exchange.model.TickerData;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketConnectionManager {
    private final ExchangeProperties properties;
    private final ObjectMapper objectMapper;
    
    @Getter
    private volatile TickerData lastUpbitTicker;
    @Getter
    private volatile TickerData lastBinanceTicker;
    
    // Rate limiting을 위한 카운터
    private final AtomicInteger upbitMessageCount = new AtomicInteger(0);
    private final AtomicInteger binanceMessageCount = new AtomicInteger(0);
    private volatile long lastUpbitResetTime = System.currentTimeMillis();
    private volatile long lastBinanceResetTime = System.currentTimeMillis();
    
    public void updateUpbitTicker(TickerData ticker) {
        if (canProcessMessage("UPBIT")) {
            this.lastUpbitTicker = ticker;
            log.info("[UPBIT] Price: {}, Volume: {}", 
                String.format("%.0f", ticker.getPrice()), 
                String.format("%.4f", ticker.getVolume()));
        }
    }
    
    public void updateBinanceTicker(TickerData ticker) {
        if (canProcessMessage("BINANCE")) {
            this.lastBinanceTicker = ticker;
            log.info("[BINANCE] Price: {}, Volume: {}", 
                String.format("%.2f", ticker.getPrice()), 
                String.format("%.4f", ticker.getVolume()));
        }
    }
    
    private boolean canProcessMessage(String exchange) {
        long currentTime = System.currentTimeMillis();
        ExchangeProperties.Exchange config = 
            "UPBIT".equals(exchange) ? properties.getUpbit() : properties.getBinance();
        AtomicInteger counter = 
            "UPBIT".equals(exchange) ? upbitMessageCount : binanceMessageCount;
        long lastResetTime = 
            "UPBIT".equals(exchange) ? lastUpbitResetTime : lastBinanceResetTime;
        
        // 1초마다 카운터 리셋
        if (currentTime - lastResetTime >= 1000) {
            counter.set(0);
            if ("UPBIT".equals(exchange)) {
                lastUpbitResetTime = currentTime;
            } else {
                lastBinanceResetTime = currentTime;
            }
        }
        
        // 초당 메시지 제한 체크
        if (counter.get() < config.getRateLimit().getMaxMessagesPerSecond()) {
            counter.incrementAndGet();
            return true;
        }
        
        return false;
    }
    
    @PostConstruct
    public void connect() {
        WebSocketClient client = new StandardWebSocketClient();
        
        try {
            client.execute(
                new UpbitWebSocketHandler(objectMapper, this),
                properties.getUpbit().getWsUrl()
            );
            log.info("Upbit WebSocket connection initiated");
            
            client.execute(
                new BinanceWebSocketHandler(objectMapper, this),
                properties.getBinance().getWsUrl()
            );
            log.info("Binance WebSocket connection initiated");
            
        } catch (Exception e) {
            log.error("Failed to connect to WebSocket", e);
            throw new RuntimeException("Failed to connect to WebSocket", e);
        }
    }
} 