package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.matching.engine.domain.OrderBook;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.matching.engine.LimitOrderBuilder.aLimitOrder;

class BuyLimitOrderFullyExecutedSerializerTest {

    private BuyLimitOrderFullyExecutedSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new BuyLimitOrderFullyExecutedSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        OrderBook.BuyLimitOrderFullyExecuted buyLimitOrderFullyExecuted = new OrderBook.BuyLimitOrderFullyExecuted(
                new UUID(0L, 1L),
                1L,
                aLimitOrder().build()
        );


        // When
        byte[] bytes = serializer.serialize(buyLimitOrderFullyExecuted);

        // Then
        OrderBook.BuyLimitOrderFullyExecuted deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(buyLimitOrderFullyExecuted);
    }

}