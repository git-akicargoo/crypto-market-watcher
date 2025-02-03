package com.example.boot_redis_kafka_mysql.crypto.exchange.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "crypto.exchange")
public class ExchangeProperties {
    private Exchange upbit = new Exchange();
    private Exchange binance = new Exchange();

    @Data
    public static class Exchange {
        private String wsUrl;
        private String subscribeFormat;
        private int reconnectDelay;
        private int maxRetryAttempts;
        private RateLimit rateLimit = new RateLimit();
    }

    @Data
    public static class RateLimit {
        private int maxConnectionsPerMinute;
        private int maxMessagesPerSecond;
        private int maxStreamsPerConnection;
    }
}