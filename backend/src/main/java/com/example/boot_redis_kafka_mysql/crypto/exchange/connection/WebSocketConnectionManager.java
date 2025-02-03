package com.example.boot_redis_kafka_mysql.crypto.exchange.connection;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.scheduling.annotation.Scheduled;
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
    
    private final AtomicInteger upbitMessageCount = new AtomicInteger(0);
    private final AtomicInteger binanceMessageCount = new AtomicInteger(0);
    private volatile long lastUpbitResetTime = System.currentTimeMillis();
    private volatile long lastBinanceResetTime = System.currentTimeMillis();
    
    private UpbitWebSocketHandler upbitHandler;
    private BinanceWebSocketHandler binanceHandler;
    
    public void updateUpbitTicker(TickerData ticker) {
        if (canProcessMessage("UPBIT")) {
            this.lastUpbitTicker = ticker;
            log.debug("[UPBIT] Price: {}, Volume: {}", 
                String.format("%.0f", ticker.getPrice()), 
                String.format("%.4f", ticker.getVolume()));
        }
    }
    
    public void updateBinanceTicker(TickerData ticker) {
        if (canProcessMessage("BINANCE")) {
            this.lastBinanceTicker = ticker;
            log.debug("[BINANCE] Price: {}, Volume: {}", 
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
        
        if (currentTime - lastResetTime >= 1000) {
            counter.set(0);
            if ("UPBIT".equals(exchange)) {
                lastUpbitResetTime = currentTime;
            } else {
                lastBinanceResetTime = currentTime;
            }
        }
        
        if (counter.get() < config.getRateLimit().getMaxMessagesPerSecond()) {
            counter.incrementAndGet();
            return true;
        }
        
        return false;
    }
    
    @PostConstruct
    public void connect() {
        log.info("Initializing WebSocket connections...");
        WebSocketClient client = new StandardWebSocketClient();
        
        try {
            // Upbit 핸들러 초기화 및 연결
            log.info("Initializing Upbit handler...");
            upbitHandler = new UpbitWebSocketHandler(objectMapper, this, properties);
            client.execute(
                upbitHandler,
                properties.getUpbit().getWsUrl()
            );
            log.info("Upbit WebSocket connection initiated");
            
            // Binance 핸들러 초기화 및 연결
            log.info("Initializing Binance handler...");
            binanceHandler = new BinanceWebSocketHandler(objectMapper, this, properties);
            client.execute(
                binanceHandler,
                properties.getBinance().getWsUrl()
            );
            log.info("Binance WebSocket connection initiated");
            
        } catch (Exception e) {
            log.error("Failed to connect to WebSocket", e);
            throw new RuntimeException("Failed to connect to WebSocket", e);
        }
    }
    
    public void subscribeUpbitMarket(String market, String type) {
        if (upbitHandler != null) {
            upbitHandler.subscribe(market, type);
        } else {
            log.error("Upbit handler is not initialized");
        }
    }
    
    public void subscribeBinanceMarket(String symbol, String type) {
        if (binanceHandler != null) {
            binanceHandler.subscribe(symbol, type);
        } else {
            log.error("Binance handler is not initialized");
        }
    }

    @Scheduled(fixedRate = 5000)  // 5초마다 실행
    public void logConnectionStatus() {
        log.info("WebSocket Connection Status:");
        log.info("Upbit: {}", upbitHandler != null && upbitHandler.isConnected() ? "Connected" : "Disconnected");
        log.info("Binance: {}", binanceHandler != null && binanceHandler.isConnected() ? "Connected" : "Disconnected");
    }
}