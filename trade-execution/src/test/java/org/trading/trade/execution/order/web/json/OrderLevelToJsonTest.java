package org.trading.trade.execution.order.web.json;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.trade.execution.order.event.OrderLevelUpdated;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.command.Side.BUY;

public class OrderLevelToJsonTest {

    private OrderLevelToJson orderLevelToJson;

    @BeforeEach
    void before() {
        orderLevelToJson = new OrderLevelToJson();
    }

    @Test
    void should_convert_order_level_to_json() {

        // Given
        final OrderLevelUpdated limitOrderPlaced = new OrderLevelUpdated(
                "EURUSD",
                BUY,
                10,
                10.8
        );

        // When
        final JSONObject jsonEvent = orderLevelToJson.toJson(limitOrderPlaced);

        // Then
        assertThat(jsonEvent.getAsString("symbol")).isEqualTo("EURUSD");
        assertThat(jsonEvent.getAsString("side")).isEqualTo("BUY");
        assertThat(jsonEvent.getAsNumber("size").intValue()).isEqualTo(10);
        assertThat(jsonEvent.getAsNumber("price").doubleValue()).isEqualTo(10.8);
    }


}