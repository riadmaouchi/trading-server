package org.trading.messaging.serializer;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.api.event.TradeExecuted;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.messaging.TradeExecutedBuilder.aTradeExecuted;

class TradeExecutedFromProtobufTest {

    private TradeExecutedFromProtobuf tradeExecutedFromProtobuf;

    @BeforeEach
    void before() {
        tradeExecutedFromProtobuf = new TradeExecutedFromProtobuf();
    }

    @Test
    void should_convert_limit_order_placed() {

        // Given
        org.trading.TradeExecuted tradeExecuted = aTradeExecuted()
                .withBuyingId("00000000-0000-0000-0000-000000000001")
                .withBuyingBroker("Buying Broker")
                .withBuyingLimit(1.23489)
                .withSellingId("00000000-0000-0001-0000-000000000002")
                .withSellingBroker("Selling Broker")
                .withSellingLimit(1.23480)
                .withPrice(1.23482)
                .withQuantity(1_000_000)
                .withTime(Timestamp.newBuilder().setSeconds(1530464460L).build())
                .withSymbol("EURUSD")
                .build();

        // When
        Either<String, TradeExecuted> tradeExecutedEither = tradeExecutedFromProtobuf.fromProtobuf(tradeExecuted);

        // then
        TradeExecuted trade = tradeExecutedEither.right();
        assertThat(trade.buyingId).isEqualTo(new UUID(0, 1));
        assertThat(trade.buyingBroker).isEqualTo("Buying Broker");
        assertThat(trade.buyingLimit).isEqualTo(1.23489);
        assertThat(trade.sellingId).isEqualTo(new UUID(1, 2));
        assertThat(trade.sellingBroker).isEqualTo("Selling Broker");
        assertThat(trade.sellingLimit).isEqualTo(1.23480);
        assertThat(trade.symbol).isEqualTo("EURUSD");
        assertThat(trade.quantity).isEqualTo(1_000_000);
        assertThat(trade.price).isEqualTo(1.23482);
        assertThat(trade.time).isEqualTo(LocalDateTime.of(2018, JULY, 1, 17, 1));
    }
}