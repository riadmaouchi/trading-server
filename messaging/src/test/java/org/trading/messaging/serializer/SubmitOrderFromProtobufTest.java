package org.trading.messaging.serializer;

import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.api.command.SubmitOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.command.OrderType.LIMIT;
import static org.trading.api.command.OrderType.MARKET;
import static org.trading.api.command.Side.BUY;
import static org.trading.api.command.Side.SELL;
import static org.trading.messaging.SubmitOrderBuilder.aLimitOrder;
import static org.trading.messaging.SubmitOrderBuilder.aMarketOrder;

@ExtendWith(DataProviderExtension.class)
class SubmitOrderFromProtobufTest {

    private SubmitOrderFromProtobuf submitOrderFromProtobuf;

    @BeforeEach
    void before() {
        submitOrderFromProtobuf = new SubmitOrderFromProtobuf(new SideFromProtobuf());
    }

    @Test
    void should_convert_market_order() {

        // Given
        org.trading.SubmitOrder submitOrder = aMarketOrder()
                .withAmount(5_000)
                .withBroker("broker")
                .withSymbol("EURUSD")
                .withSide(org.trading.Side.BUY)
                .build();

        // When
        Either<String, SubmitOrder> submitOrderEither = submitOrderFromProtobuf.fromProtobuf(submitOrder);

        // then
        SubmitOrder marketOrder = submitOrderEither.right();
        assertThat(marketOrder.symbol).isEqualTo("EURUSD");
        assertThat(marketOrder.amount).isEqualTo(5_000);
        assertThat(marketOrder.broker).isEqualTo("broker");
        assertThat(marketOrder.orderType).isEqualTo(MARKET);
        assertThat(marketOrder.side).isEqualTo(BUY);
    }

    @Test
    void should_convert_limit_order() {

        // Given
        org.trading.SubmitOrder submitOrder = aLimitOrder()
                .withAmount(5_000)
                .withBroker("broker")
                .withSymbol("EURUSD")
                .withPrice(1.2344)
                .withSide(org.trading.Side.SELL)
                .build();

        // When
        Either<String, SubmitOrder> submitOrderEither = submitOrderFromProtobuf.fromProtobuf(submitOrder);

        // then
        SubmitOrder marketOrder = submitOrderEither.right();
        assertThat(marketOrder.symbol).isEqualTo("EURUSD");
        assertThat(marketOrder.amount).isEqualTo(5_000);
        assertThat(marketOrder.broker).isEqualTo("broker");
        assertThat(marketOrder.orderType).isEqualTo(LIMIT);
        assertThat((marketOrder.limit)).isEqualTo(1.2344);
        assertThat(marketOrder.side).isEqualTo(SELL);
    }
}