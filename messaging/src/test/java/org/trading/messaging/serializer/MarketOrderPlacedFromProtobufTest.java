package org.trading.messaging.serializer;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.Side;
import org.trading.api.event.MarketOrderPlaced;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.command.Side.BUY;
import static org.trading.messaging.MarketOrderPlacedBuilder.aMarketOrderPlaced;

class MarketOrderPlacedFromProtobufTest {

    private MarketOrderPlacedFromProtobuf marketOrderPlacedFromProtobuf;

    @BeforeEach
    void before() {
        marketOrderPlacedFromProtobuf = new MarketOrderPlacedFromProtobuf(new SideFromProtobuf());
    }

    @Test
    void should_convert_limit_order_placed() {

        // Given
        org.trading.MarketOrderPlaced marketOrderPlaced = aMarketOrderPlaced()
                .withBroker("broker")
                .withSymbol("EURUSD")
                .withSide(Side.BUY)
                .withId("00000000-0000-0001-0000-000000000002")
                .withQuantity(5_000)
                .withTime(Timestamp.newBuilder().setSeconds(1530464460L).build())
                .build();

        // When
        Either<String, MarketOrderPlaced> marketOrderPlacedEither = marketOrderPlacedFromProtobuf.fromProtobuf(marketOrderPlaced);

        // then
        MarketOrderPlaced orderPlaced = marketOrderPlacedEither.right();
        assertThat(orderPlaced.symbol).isEqualTo("EURUSD");
        assertThat(orderPlaced.quantity).isEqualTo(5_000);
        assertThat(orderPlaced.broker).isEqualTo("broker");
        assertThat(orderPlaced.id).isEqualTo(new UUID(1, 2));
        assertThat(orderPlaced.side).isEqualTo(BUY);
        assertThat(orderPlaced.time).isEqualTo(LocalDateTime.of(2018, JULY, 1, 17, 1));
    }

}