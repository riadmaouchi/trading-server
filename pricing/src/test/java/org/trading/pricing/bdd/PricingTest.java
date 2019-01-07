package org.trading.pricing.bdd;

import com.google.common.collect.Iterables;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.Format;
import com.tngtech.jgiven.annotation.Table;
import com.tngtech.jgiven.junit5.SimpleScenarioTest;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.trading.api.TradeExecutedBuilder;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.pricing.bdd.format.CollectionFormatter;
import org.trading.pricing.bdd.model.Order;
import org.trading.pricing.bdd.model.Prices;
import org.trading.pricing.domain.Price;
import org.trading.pricing.domain.PriceListener;
import org.trading.pricing.domain.PricingService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.trading.api.TradeExecutedBuilder.aTradeExecuted;
import static org.trading.pricing.bdd.model.OrderBuilder.*;

class PricingTest extends SimpleScenarioTest<PricingTest.PricingTestSteps> {

    @Test
    void ask_ladder_prices_are_computed_with_sell_remaining_unfulfilled_orders() {

        given().the_ladder_quantities_are_defined_such_as(new IntArrayList(new int[]{1_000_000, 5_000_000}));

        when().the_following_orders_have_been_submitted_in_this_order(
                aSellLimitOrder().quantity("200000").price("1.24").build(),
                aSellLimitOrder().quantity("500000").price("1.20").build(),
                aSellLimitOrder().quantity("300000").price("1.22").build(),
                aSellLimitOrder().quantity("200000").price("1.21").build()
        );

        then().the_price_ladder_look_like(
                new Prices("1000000", "", "1.208")
        );
    }

    @Test
    void bid_ladder_prices_are_computed_with_ask_remaining_unfulfilled_orders() {

        given().the_ladder_quantities_are_defined_such_as(new IntArrayList(new int[]{1_000_000, 5_000_000}));

        when().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().quantity("500000").price("1.20").build(),
                aBuyLimitOrder().quantity("200000").price("1.21").build(),
                aBuyLimitOrder().quantity("200000").price("1.19").build(),
                aBuyLimitOrder().quantity("300000").price("1.22").build()
        );

        then().the_price_ladder_look_like(
                new Prices("1000000", "1.208", "")
        );
    }

    @Test
    void ladder_prices_are_computed_with_remaining_unfulfilled_orders() {

        given().the_ladder_quantities_are_defined_such_as(new IntArrayList(new int[]{1_000_000, 5_000_000, 10_000_000}))
                .and().the_following_orders_have_been_submitted_in_this_order(
                aSellLimitOrder().quantity("1000000").price("1.17").build(),
                aSellLimitOrder().quantity("2000000").price("1.18").build(),
                aSellLimitOrder().quantity("7000000").price("1.19").build(),
                aBuyLimitOrder().quantity("1000000").price("1.20").build(),
                aBuyLimitOrder().quantity("2000000").price("1.21").build(),
                aBuyLimitOrder().quantity("8000000").price("1.22").build()
        );

        when().the_following_orders_are_submitted_in_this_order(
                aBuyLimitOrder().quantity("1000000").price("1.19").build()
        );

        then().the_price_ladder_look_like(
                new Prices("1000000", "1.22", "1.17"),
                new Prices("5000000", "1.22", "1.182"),
                new Prices("10000000", "1.218", "")
        );
    }

    @Test
    void ladder_prices_are_not_computed_when_remaining_unfulfilled_orders_liquidity_is_insufficient() {

        given().the_ladder_quantities_are_defined_such_as(new IntArrayList(new int[]{1_000_000, 5_000_000}));

        when().the_following_orders_have_been_submitted_in_this_order(
                aLimitOrder().quantity("500000").build()
        );

        then().no_price_is_computed();
    }

    static class PricingTestSteps extends Stage<PricingTestSteps> {

        private PricingService pricingService;

        private PriceListener priceListener;
        private ArgumentCaptor<org.trading.pricing.domain.Prices> priceArgumentCaptor;

        @BeforeStage
        public void before() {
            priceListener = mock(PriceListener.class);
            priceArgumentCaptor = forClass(org.trading.pricing.domain.Prices.class);
        }

        PricingTestSteps the_ladder_quantities_are_defined_such_as(@Format(CollectionFormatter.class) List<Integer> quantities) {
            pricingService = new PricingService(priceListener, quantities);
            return this;
        }

        void the_following_orders_have_been_submitted_in_this_order(@Table(columnTitles = {"Side", "Qty", "Prices"},
                excludeFields = {"broker", "symbol"}) Order... orders) {
            IntStream.range(0, orders.length)
                    .mapToObj(id -> {
                        final Order order = orders[id];
                        return new LimitOrderPlaced(
                                new UUID(0, id),
                                LocalDateTime.now(),
                                order.broker,
                                parseInt(order.quantity),
                                order.side,
                                parseDouble(order.price),
                                order.symbol
                        );
                    }).forEach(pricingService::onLimitOrderPlaced);
        }

        void the_following_orders_are_submitted_in_this_order(@Table(columnTitles = {"Side", "Qty", "Prices"},
                excludeFields = {"broker", "symbol"}) Order... orders) {
            the_following_orders_have_been_submitted_in_this_order(orders);
            Arrays.stream(orders).map(o -> aTradeExecuted()
                    .withSymbol(o.symbol)
                    .withQuantity(parseInt(o.quantity))
                    .withPrice(parseDouble(o.price))
                    .withBuyingLimit(parseDouble(o.price))
                    .withSellingLimit(parseDouble(o.price))
                    .build()).forEach(pricingService::onTradeExecuted);
        }

        void no_price_is_computed() {
            verify(priceListener, never()).onPrices(any(org.trading.pricing.domain.Prices.class));
        }

        void the_price_ladder_look_like(@Table Prices... prices) {
            verify(priceListener, atLeastOnce()).onPrices(priceArgumentCaptor.capture());
            final org.trading.pricing.domain.Prices price = Iterables.getLast(priceArgumentCaptor.getAllValues());

            Price[] bids = Stream.of(prices)
                    .filter(p -> !p.bid.isEmpty())
                    .map(p -> new Price(parseInt(p.volume), parseDouble(p.bid)))
                    .toArray(Price[]::new);

            Price[] asks = Stream.of(prices)
                    .filter(p -> !p.ask.isEmpty())
                    .map(p -> new Price(parseInt(p.volume), parseDouble(p.ask)))
                    .toArray(Price[]::new);

            assertThat(price.asks).containsExactly(asks);
            assertThat(price.bids).containsExactly(bids);
        }
    }
}