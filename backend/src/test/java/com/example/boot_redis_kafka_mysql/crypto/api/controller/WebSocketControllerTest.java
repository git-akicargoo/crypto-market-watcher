package com.example.boot_redis_kafka_mysql.crypto.api.controller;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.boot_redis_kafka_mysql.crypto.api.dto.request.SubscribeRequest;
import com.example.boot_redis_kafka_mysql.crypto.api.service.CryptoDataService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
class WebSocketControllerTest {

    @Mock
    private CryptoDataService cryptoDataService;

    @InjectMocks
    private WebSocketController webSocketController;

    @Test
    void testSubscribe() {
        // Given
        SubscribeRequest request = new SubscribeRequest();
        request.setExchange("UPBIT");
        request.setMarket("KRW-BTC");
        request.setType("ticker");

        // When
        webSocketController.subscribe(request);

        // Then
        verify(cryptoDataService).subscribe(request);
    }
}