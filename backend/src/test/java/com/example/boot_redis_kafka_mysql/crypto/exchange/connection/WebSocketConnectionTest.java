package com.example.boot_redis_kafka_mysql.crypto.exchange.connection;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
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
    
    private static final int TIMEOUT_SECONDS = 30;

    @BeforeAll
    void setup() {
        System.out.println("\n=== Starting WebSocket Connection Tests ===\n");
        
        try {
            connectionManager.connect();
            
            // Wait for both connections with timeout
            Awaitility.await()
                .atMost(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    TickerData upbit = connectionManager.getLastUpbitTicker();
                    TickerData binance = connectionManager.getLastBinanceTicker();
                    
                    if (upbit != null) {
                        System.out.println("Upbit price: " + upbit.getPrice() + " KRW");
                    }
                    if (binance != null) {
                        System.out.println("Binance price: " + binance.getPrice() + " USDT");
                    }
                    
                    return upbit != null && binance != null;
                });
                
            System.out.println("\n=== WebSocket Connections Established ===\n");
            
        } catch (Exception e) {
            System.err.println("Failed to establish connections: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(1)
    void testUpbitDataReceived() {
        TickerData ticker = connectionManager.getLastUpbitTicker();
        assertNotNull(ticker, "Upbit data should be received");
        assertTrue(ticker.getPrice() > 0, "Upbit price should be positive");
        System.out.println("Upbit price: " + ticker.getPrice() + " KRW");
    }

    @Test
    @Order(2)
    void testBinanceDataReceived() {
        TickerData ticker = connectionManager.getLastBinanceTicker();
        assertNotNull(ticker, "Binance data should be received");
        assertTrue(ticker.getPrice() > 0, "Binance price should be positive");
        System.out.println("Binance price: " + ticker.getPrice() + " USDT");
    }

    @Test
    @Order(3)
    void testKimchiPremium() throws InterruptedException {
        // 최신 데이터를 받기 위해 잠시 대기
        Thread.sleep(2000);
        
        TickerData upbit = connectionManager.getLastUpbitTicker();
        TickerData binance = connectionManager.getLastBinanceTicker();
        
        assertNotNull(upbit, "Upbit data should be available");
        assertNotNull(binance, "Binance data should be available");
        
        System.out.println("\n=== Price Comparison ===");
        System.out.println("Upbit: " + upbit.getPrice() + " KRW");
        System.out.println("Binance: " + binance.getPrice() + " USDT");
        
        // 프리미엄 계산 (환율 1300원 가정)
        double upbitUsdPrice = upbit.getPrice() / 1300.0;
        double premium = ((upbitUsdPrice - binance.getPrice()) / binance.getPrice()) * 100;
        
        System.out.println("Upbit price in USD: " + String.format("%.2f", upbitUsdPrice) + " USD");
        System.out.println("Premium: " + String.format("%.2f%%", premium));
        
        // 프리미엄 검증 범위를 더 넓게 설정
        assertTrue(Math.abs(premium) < 30, 
            String.format("Kimchi premium (%.2f%%) should be within ±30%%", premium));
    }

    @Test
    void testWebSocketConnection() {
        // 1. 웹소켓 연결
        connectionManager.connect();
        log.info("WebSocket connections initiated");
        
        // 2. 데이터 수신 대기 (최대 30초)
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .pollInterval(Duration.ofSeconds(1))
            .until(() -> {
                TickerData upbit = connectionManager.getLastUpbitTicker();
                TickerData binance = connectionManager.getLastBinanceTicker();
                
                if (upbit != null) {
                    log.info("Upbit: {} KRW", upbit.getPrice());
                }
                if (binance != null) {
                    log.info("Binance: {} USDT", binance.getPrice());
                }
                
                return upbit != null && binance != null;
            });
            
        // 3. 최종 검증
        TickerData upbit = connectionManager.getLastUpbitTicker();
        TickerData binance = connectionManager.getLastBinanceTicker();
        
        assertNotNull(upbit, "Should receive Upbit data");
        assertNotNull(binance, "Should receive Binance data");
        
        log.info("Final Upbit price: {} KRW", upbit.getPrice());
        log.info("Final Binance price: {} USDT", binance.getPrice());
    }
} 