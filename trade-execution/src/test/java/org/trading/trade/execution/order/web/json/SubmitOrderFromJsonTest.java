package org.trading.trade.execution.order.web.json;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import net.minidev.json.JSONObject;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.OrderType;
import org.trading.Side;
import org.trading.SubmitOrder;
import org.trading.trade.execution.SubmitOrderBuilder;

import static java.lang.Double.NaN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(DataProviderExtension.class)
public class SubmitOrderFromJsonTest {

    private SubmitOrderFromJson submitOrderFromJson;

    @BeforeEach
    public void before() {
        submitOrderFromJson = new SubmitOrderFromJson();
    }

    @Test
    public void should_convert_limit_order_request_from_json() {

        // Given
        final JSONObject json = SubmitOrderBuilder.aSubmitOrder().withId(1L)
                .withSymbol("EURUSD")
                .withBroker("A")
                .withAmount(12)
                .withType("limit")
                .withPrice(14.89)
                .build();

        // When
        final SubmitOrder submitOrder = submitOrderFromJson.fromJson(json);

        // Then
        assertThat(submitOrder.getSymbol()).isEqualTo("EURUSD");
        assertThat(submitOrder.getAmount()).isEqualTo(12);
        AssertionsForClassTypes.assertThat(submitOrder.getOrderType()).isEqualTo(OrderType.LIMIT);
        assertThat(submitOrder.getPrice()).isEqualTo(14.89);

    }

    @Test
    public void should_convert_market_order_request_from_json() {

        // Given
        final JSONObject json = SubmitOrderBuilder.aSubmitOrder().withId(1L)
                .withSymbol("EURUSD")
                .withBroker("A")
                .withAmount(12)
                .withType("market")
                .build();

        // When
        final SubmitOrder submitOrder = submitOrderFromJson.fromJson(json);

        // Then
        assertThat(submitOrder.getSymbol()).isEqualTo("EURUSD");
        assertThat(submitOrder.getAmount()).isEqualTo(12);
        AssertionsForClassTypes.assertThat(submitOrder.getOrderType()).isEqualTo(OrderType.MARKET);
        assertThat(submitOrder.getPrice()).isEqualTo(NaN);
    }

    @DataProvider( {
            "buy, BUY",
            "sell, SELL"
    })
    @TestTemplate
    public void should_convert_market_order_request_from_json(String inputSide, Side outputSide) {

        // Given
        final JSONObject json = SubmitOrderBuilder.aSubmitOrder().withSide(inputSide).build();

        // When
        final SubmitOrder submitOrder = submitOrderFromJson.fromJson(json);

        // Then
        AssertionsForClassTypes.assertThat(submitOrder.getSide()).isEqualTo(outputSide);
    }
}