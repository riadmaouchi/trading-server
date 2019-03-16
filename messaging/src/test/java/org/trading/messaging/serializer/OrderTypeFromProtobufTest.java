package org.trading.messaging.serializer;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.MessageProvider;
import org.trading.api.message.OrderType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.MessageProvider.OrderType.UNKNOWN_ORDER_TYPE;

@ExtendWith(DataProviderExtension.class)
class OrderTypeFromProtobufTest {

    private OrderTypeFromProtobuf orderTypeFromProtobuf;

    @BeforeEach
    void before() {
        orderTypeFromProtobuf = new OrderTypeFromProtobuf();
    }

    @DataProvider({
            "MARKET, MARKET",
            "LIMIT, LIMIT",
    })
    @TestTemplate
    void should_convert_order_side(MessageProvider.OrderType inputOrderType, OrderType outputOrderSide) {

        // When
        Either<String, OrderType> orderTypeEither = orderTypeFromProtobuf.visit(inputOrderType, inputOrderType);

        // then
        OrderType orderType = orderTypeEither.right();
        assertThat(orderType).isEqualTo(outputOrderSide);
    }


    @Test
    void should_fail_to_convert_unknown_order_type() {

        // When
        Either<String, OrderType> orderTypeEither = orderTypeFromProtobuf.visit(UNKNOWN_ORDER_TYPE, UNKNOWN_ORDER_TYPE);

        // then
        String reason = orderTypeEither.left();
        assertThat(reason).isEqualTo("Unknown orderType : UNKNOWN_ORDER_TYPE");
    }

}