package org.trading.trade.execution.order.web.json;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.api.event.TradeExecuted;
import org.trading.trade.execution.order.event.LastTradeExecuted;

import java.time.LocalDateTime;

import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.trade.execution.TradeExecutedBuilder.aTradeExecuted;

public class LastTradeToJsonTest {

    private LastTradeToJson lastTradeToJson;

    @BeforeEach
    public void before() {
        lastTradeToJson = new LastTradeToJson();
    }


    @Test
    public void should_convert_last_trade_to_json() {

        // Given
        LastTradeExecuted lastTradeExecuted = new LastTradeExecuted(
                "EURUSD",
                1.3233,
                100,
                LocalDateTime.of(2018, JUNE, 3, 11, 5, 30),
                1.232,
                1.2322,
                1.1231,
                1.2232
        );

        // When
        JSONObject lastTrade = lastTradeToJson.toJson(lastTradeExecuted);

        // Then
        assertThat(lastTrade.getAsString("symbol")).isEqualTo("EURUSD");
        assertThat(lastTrade.getAsNumber("lastPrice").doubleValue()).isEqualTo(1.3233);
        assertThat(lastTrade.getAsNumber("lastQuantity").intValue()).isEqualTo(100);
        assertThat(lastTrade.getAsString("time")).isEqualTo("2018-06-03T11:05:30");
        assertThat(lastTrade.getAsNumber("open").doubleValue()).isEqualTo(1.232);
        assertThat(lastTrade.getAsNumber("high").doubleValue()).isEqualTo(1.2322);
        assertThat(lastTrade.getAsNumber("low").doubleValue()).isEqualTo(1.1231);
        assertThat(lastTrade.getAsNumber("close").doubleValue()).isEqualTo(1.2232);
    }
}