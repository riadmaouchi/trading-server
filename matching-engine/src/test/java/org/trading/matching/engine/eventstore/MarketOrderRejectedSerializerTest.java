package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.api.message.FillStatus;
import org.trading.matching.engine.domain.OrderBook.MarketOrderRejected;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.matching.engine.MarketOrderBuilder.aMarketOrder;

class MarketOrderRejectedSerializerTest {

    private MarketOrderRejectedSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new MarketOrderRejectedSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        MarketOrderRejected marketOrderRejected = new MarketOrderRejected(
                new UUID(0L, 1L),
                1L,
                aMarketOrder().build(),
                FillStatus.PARTIALLY_FILLED
        );

        // When
        byte[] bytes = serializer.serialize(marketOrderRejected);

        // Then
        MarketOrderRejected deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(marketOrderRejected);
    }

}