package org.trading.messaging.serializer;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.MessageProvider;
import org.trading.MessageProvider.Side;
import org.trading.api.event.MarketOrderAccepted;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.message.Side.BUY;
import static org.trading.messaging.MarketOrderAcceptedBuilder.aMarketOrderAccepted;

class MarketOrderAcceptedFromProtobufTest {

    private MarketOrderAcceptedFromProtobuf marketOrderAcceptedFromProtobuf;

    @BeforeEach
    void before() {
        marketOrderAcceptedFromProtobuf = new MarketOrderAcceptedFromProtobuf(new SideFromProtobuf());
    }

    @Test
    void should_convert_limit_order_placed() {

        // Given
        MessageProvider.MarketOrderAccepted marketOrderAccepted = aMarketOrderAccepted()
                .withBroker("broker")
                .withSymbol("EURUSD")
                .withSide(Side.BUY)
                .withId("00000000-0000-0001-0000-000000000002")
                .withQuantity(5_000)
                .withTime(Timestamp.newBuilder().setSeconds(1530464460L).build())
                .build();

        // When
        Either<String, MarketOrderAccepted> marketOrderAcceptedEither = marketOrderAcceptedFromProtobuf.fromProtobuf(marketOrderAccepted);

        // then
        MarketOrderAccepted orderAccepted = marketOrderAcceptedEither.right();
        assertThat(orderAccepted.symbol).isEqualTo("EURUSD");
        assertThat(orderAccepted.quantity).isEqualTo(5_000);
        assertThat(orderAccepted.broker).isEqualTo("broker");
        assertThat(orderAccepted.id).isEqualTo(new UUID(1, 2));
        assertThat(orderAccepted.side).isEqualTo(BUY);
        assertThat(orderAccepted.time).isEqualTo(LocalDateTime.of(2018, JULY, 1, 17, 1));
    }

}