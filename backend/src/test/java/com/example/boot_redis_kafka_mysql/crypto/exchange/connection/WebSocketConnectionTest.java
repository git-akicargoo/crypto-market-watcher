package com.example.boot_redis_kafka_mysql.crypto.exchange.connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.boot_redis_kafka_mysql.crypto.exchange.model.TickerData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class WebSocketConnectionTest {

    @Autowired
    private WebSocketConnectionManager connectionManager;
    
    @BeforeAll
    void setup() {
        log.info("=== Starting WebSocket Connection Tests ===");
        connectionManager.connect();
        // 연결 안정화를 위한 대기
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testWebSocketSubscription() {
        // 업비트 테스트
        log.info("=== Testing Upbit Subscription ===");
        String market = "KRW-BTC";
        connectionManager.subscribeUpbitMarket(market, "ticker");
        
        // 데이터 수신 확인 (5초 동안)
        long startTime = System.currentTimeMillis();
        int messageCount = 0;
        while (System.currentTimeMillis() - startTime < 5000) {
            TickerData ticker = connectionManager.getLastUpbitTicker();
            if (ticker != null && market.equals(ticker.getSymbol())) {
                log.info("Upbit {} - Price: {}", market, ticker.getPrice());
                messageCount++;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        assertTrue(messageCount > 0, "Should receive multiple Upbit messages");
        
        // 바이낸스 테스트
        log.info("=== Testing Binance Subscription ===");
        String symbol = "BTC-USDT";
        connectionManager.subscribeBinanceMarket(symbol, "ticker");
        
        // 데이터 수신 확인 (5초 동안)
        startTime = System.currentTimeMillis();
        messageCount = 0;
        while (System.currentTimeMillis() - startTime < 5000) {
            TickerData ticker = connectionManager.getLastBinanceTicker();
            if (ticker != null && symbol.replace("-", "").equals(ticker.getSymbol())) {
                log.info("Binance {} - Price: {}", symbol, ticker.getPrice());
                messageCount++;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        assertTrue(messageCount > 0, "Should receive multiple Binance messages");
    }
} 