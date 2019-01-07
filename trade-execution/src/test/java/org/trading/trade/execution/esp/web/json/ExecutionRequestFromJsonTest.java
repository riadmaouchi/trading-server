package org.trading.trade.execution.esp.web.json;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.trade.execution.esp.domain.ExecutionRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.command.Side.BUY;

public class ExecutionRequestFromJsonTest {

    private ExecutionRequestFromJson executionRequestFromJson;

    @BeforeEach
    public void before() {
        executionRequestFromJson = new ExecutionRequestFromJson();
    }

    @Test
    public void should_convert_execution_request_from_json() {

        // Given
        final JSONObject json = new JSONObject();
        json.put("broker", "aBroker");
        json.put("quantity", 25);
        json.put("side", "buy");
        json.put("symbol", "EURUSD");
        json.put("price", 1.17289);

        // When
        ExecutionRequest executionRequest = executionRequestFromJson.toJson(json);

        // Then
        assertThat(executionRequest.broker).isEqualTo("aBroker");
        assertThat(executionRequest.quantity).isEqualTo(25);
        assertThat(executionRequest.side).isEqualTo(BUY);
        assertThat(executionRequest.symbol).isEqualTo("EURUSD");
        assertThat(executionRequest.price).isEqualTo(1.17289);
    }

}