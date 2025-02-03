package com.example.boot_redis_kafka_mysql.crypto.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.example.boot_redis_kafka_mysql.crypto.api.dto.request.SubscribeRequest;
import com.example.boot_redis_kafka_mysql.crypto.api.service.CryptoDataService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CryptoWebSocketControllerTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private CryptoDataService cryptoDataService;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new SockJsClient(
            List.of(new WebSocketTransport(new StandardWebSocketClient()))
        ));
    }

    @Test
    void testSubscribe() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        String websocketUrl = String.format("ws://localhost:%d/ws", port);
        
        StompSession session = stompClient
            .connectAsync(websocketUrl, new StompSessionHandlerAdapter() {})
            .get(1, TimeUnit.SECONDS);

        SubscribeRequest request = new SubscribeRequest();
        request.setExchange("UPBIT");
        request.setMarket("KRW-BTC");

        // When
        session.send("/publish/ticker/subscribe", request);

        // Then
        verify(cryptoDataService, timeout(1000)).subscribe(any(SubscribeRequest.class));
    }
} 