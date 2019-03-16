package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.matching.engine.domain.OrderBook;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBookCreatedSerializerTest {
    private OrderBookCreatedSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new OrderBookCreatedSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        OrderBook.OrderBookCreated orderBookCreated = new OrderBook.OrderBookCreated(
                new UUID(0L, 1L),
                1L
        );

        // When
        byte[] bytes = serializer.serialize(orderBookCreated);

        // Then
        OrderBook.OrderBookCreated deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(orderBookCreated);
    }
}