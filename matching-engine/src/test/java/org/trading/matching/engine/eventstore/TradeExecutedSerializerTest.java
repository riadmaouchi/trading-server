package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.matching.engine.TradeBuilder;
import org.trading.matching.engine.domain.OrderBook;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TradeExecutedSerializerTest {

    private TradeExecutedSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new TradeExecutedSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        OrderBook.TradeExecuted tradeExecuted = new OrderBook.TradeExecuted(
                new UUID(0L, 1L),
                1L,
                TradeBuilder.aTrade().build()
        );

        // When
        byte[] bytes = serializer.serialize(tradeExecuted);

        // Then
        OrderBook.TradeExecuted deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(tradeExecuted);
    }

}