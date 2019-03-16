package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.matching.engine.domain.OrderBook.MarketOrderAccepted;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.matching.engine.MarketOrderBuilder.aMarketOrder;

class MarketOrderAcceptedSerializerTest {

    private MarketOrderAcceptedSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new MarketOrderAcceptedSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        MarketOrderAccepted marketOrderAccepted = new MarketOrderAccepted(
                new UUID(0L, 1L),
                1L,
                aMarketOrder().build()
        );


        // When
        byte[] bytes = serializer.serialize(marketOrderAccepted);

        // Then
        MarketOrderAccepted deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(marketOrderAccepted);
    }

}