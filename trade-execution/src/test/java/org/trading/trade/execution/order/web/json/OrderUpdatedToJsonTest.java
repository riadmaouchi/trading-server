package org.trading.trade.execution.order.web.json;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.trade.execution.order.event.OrderUpdated;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.command.Side.BUY;
import static org.trading.trade.execution.order.event.OrderUpdated.Status.WORKING;
import static org.trading.trade.execution.order.event.OrderUpdated.Type.LIMIT;

class OrderUpdatedToJsonTest {

    private OrderUpdatedToJson orderUpdatedToJson;

    @BeforeEach
    void before() {
        orderUpdatedToJson = new OrderUpdatedToJson();
    }

    @Test
    void should_convert_execution_accepted_to_json() {
        // Given
        OrderUpdated orderUpdated = new OrderUpdated(
                new UUID(0, 1),
                LocalDateTime.of(2018, JUNE, 3, 11, 5, 30),
                "broker",
                10,
                2,
                8,
                BUY,
                1.342,
                1.334,
                "EURUSD",
                WORKING,
                LIMIT
        );

        // When
        JSONObject json = orderUpdatedToJson.toJson(orderUpdated);

        // Then
        assertThat(json.getAsString("id")).isEqualTo(new UUID(0, 1).toString());
        assertThat(json.getAsString("symbol")).isEqualTo("EURUSD");
        assertThat(json.getAsString("direction")).isEqualTo("BUY");
        assertThat(json.getAsString("broker")).isEqualTo("broker");
        assertThat(json.getAsString("time")).isEqualTo("2018-06-03T11:05:30");
        assertThat(json.getAsNumber("requestedAmount")).isEqualTo(10);
        assertThat(json.getAsNumber("leftAmount")).isEqualTo(2);
        assertThat(json.getAsNumber("amount")).isEqualTo(8);
        assertThat(json.getAsString("type")).isEqualTo("LIMIT");
        assertThat(json.getAsNumber("limit")).isEqualTo(1.342);
        assertThat(json.getAsNumber("price")).isEqualTo(1.334);
        assertThat(json.getAsString("status")).isEqualTo("WORKING");
    }
}