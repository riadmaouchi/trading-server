package org.trading.trade.execution.esp.web.json;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;

import java.time.LocalDateTime;

import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.command.Side.BUY;

class ExecutionAcceptedToJsonTest {
    private ExecutionAcceptedToJson executionAcceptedToJson;

    @BeforeEach
    void before() {
        executionAcceptedToJson = new ExecutionAcceptedToJson();
    }

    @Test
    void should_convert_execution_accepted_to_json() {

        // Given
        ExecutionAccepted executionAccepted = new ExecutionAccepted(
                LocalDateTime.of(2018, JULY, 1, 17, 1),
                "ID-100000000",
                "EURUSD",
                BUY,
                1.323,
                1_000,
                "Broker"
        );


        // When
        JSONObject json = executionAcceptedToJson.toJson(executionAccepted);

        // Then
        assertThat(json.get("tradeDate")).isEqualTo("2018-07-01T17:01:00");
        assertThat(json.get("id")).isEqualTo("ID-100000000");
        assertThat(json.get("symbol")).isEqualTo("EURUSD");
        assertThat(json.get("direction")).isEqualTo("BUY");
        assertThat(json.get("status")).isEqualTo("ACCEPTED");
        assertThat(json.get("price")).isEqualTo(1.323);
        assertThat(json.get("quantity")).isEqualTo(1_000);
        assertThat(json.get("broker")).isEqualTo("Broker");
    }
}