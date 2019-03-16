package org.trading.messaging.serializer;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.MessageProvider;
import org.trading.MessageProvider.OrderType;
import org.trading.api.message.Side;
import org.trading.api.message.SubmitOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.SubmitOrderBuilder.aMarketOrder;

@ExtendWith(DataProviderExtension.class)
class SubmitOrderToProtobufTest {

    private SubmitOrderToProtobuf submitOrderToProtobuf;

    @BeforeEach
    void before() {
        submitOrderToProtobuf = new SubmitOrderToProtobuf();
    }

    @Test
    void should_convert_submit_market_order() {
        // Given
        SubmitOrder order = aMarketOrder()
                .withAmount(100_000)
                .withBroker("Broker")
                .withSymbol("EURUSD")
                .build();

        // When
        MessageProvider.SubmitOrder submitOrder = submitOrderToProtobuf.toProtobuf(order);

        // Then
        assertThat(submitOrder.getAmount()).isEqualTo(100_000);
        assertThat(submitOrder.getBroker()).isEqualTo("Broker");
        assertThat(submitOrder.getPrice()).isZero();
        assertThat(submitOrder.getSymbol()).isEqualTo("EURUSD");
        assertThat(submitOrder.getOrderType()).isEqualTo(OrderType.MARKET);
    }

    @DataProvider({
            "BUY, BUY",
            "SELL, SELL",
    })
    @TestTemplate
    void should_convert_submit_market_order_side(Side inputSide, MessageProvider.Side outputSide) {
        // Given
        SubmitOrder order = aMarketOrder()
                .withSide(inputSide)
                .build();

        // When
        MessageProvider.SubmitOrder submitOrder = submitOrderToProtobuf.toProtobuf(order);

        // Then
        assertThat(submitOrder.getSide()).isEqualTo(outputSide);
    }
}