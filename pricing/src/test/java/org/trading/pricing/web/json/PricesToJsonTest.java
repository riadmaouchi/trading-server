package org.trading.pricing.web.json;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.pricing.domain.Price;
import org.trading.pricing.domain.Prices;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;

class PricesToJsonTest {

    private PricesToJson toJson;

    @BeforeEach
    void before() {
        toJson = new PricesToJson();
    }

    @Test
    void should_convert_prices_to_json() {

        // Given
        Prices prices = new Prices(
                "EURUSD",
                LocalDateTime.of(LocalDate.of(2018, JUNE, 25), LocalTime.of(15, 46, 34)),
                List.of(new Price(1000, 1.23453), new Price(2000, 1.23451)),
                List.of(new Price(1000, 1.87287), new Price(2000, 1.88288)),
                1.32);

        // When
        JSONObject json = toJson.toJson(prices);

        // Then
        assertThat(json.getAsString("symbol")).isEqualTo("EURUSD");
        assertThat(json.getAsString("time")).isEqualTo("2018-06-25T15:46:34");
        assertThat(json.getAsNumber("mid").doubleValue()).isEqualTo(1.32);

        JSONArray bids = (JSONArray) json.get("bids");
        assertThat(bids).extracting("quantity").containsExactly(1000, 2000);
        assertThat(bids).extracting("price").containsExactly(1.23453, 1.23451);
        JSONArray asks = (JSONArray) json.get("asks");
        assertThat(asks).extracting("quantity").containsExactly(1000, 2000);
        assertThat(asks).extracting("price").containsExactly(1.87287, 1.88288);
    }

    @Test
    void should_not_convert_invalid_prices() {

        // Given
        Prices prices = new Prices(
                "EURUSD",
                LocalDateTime.of(LocalDate.of(2018, JUNE, 25), LocalTime.of(15, 46, 34)),
                List.of(new Price(1000, Double.NaN)),
                List.of(new Price(1000, Double.NaN)),
                -1);

        // When
        JSONObject json = toJson.toJson(prices);

        // Then
        assertThat(json.getAsString("symbol")).isEqualTo("EURUSD");
        assertThat(json.getAsString("time")).isEqualTo("2018-06-25T15:46:34");

        assertThat((JSONArray) json.get("bids")).isEmpty();
        assertThat((JSONArray) json.get("asks")).isEmpty();
    }

}