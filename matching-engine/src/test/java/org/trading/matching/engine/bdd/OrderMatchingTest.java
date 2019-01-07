package org.trading.matching.engine.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.Table;
import com.tngtech.jgiven.junit5.SimpleScenarioTest;
import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.trading.api.command.OrderType.OrderTypeVisitor;
import org.trading.api.command.SubmitOrder;
import org.trading.api.event.TradeExecuted;
import org.trading.api.service.OrderEventListener;
import org.trading.matching.engine.bdd.model.Book;
import org.trading.matching.engine.bdd.model.Order;
import org.trading.matching.engine.bdd.tag.LimitOrder;
import org.trading.matching.engine.bdd.tag.MarketOrder;
import org.trading.matching.engine.bdd.tag.MatchingOrders;
import org.trading.matching.engine.bdd.tag.Trade;
import org.trading.matching.engine.domain.MatchingEngine;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.time.Clock.fixed;
import static java.time.Month.JANUARY;
import static java.time.ZoneOffset.UTC;
import static java.util.Optional.ofNullable;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.trading.api.command.SubmitOrder.aSubmitLimitOrder;
import static org.trading.api.command.SubmitOrder.aSubmitMarketOrder;
import static org.trading.matching.engine.bdd.model.Order.OrderRowBuilder.*;

@MatchingOrders
@Trade
@ExtendWith(DataProviderExtension.class)
class OrderMatchingTest extends SimpleScenarioTest<OrderMatchingTest.OrderMatchingTestSteps> {

    @LimitOrder
    @DataProvider({
            "10.7, 10.7",
            "10.8, 10.7"
    })
    @TestTemplate
    void match_a_single_buy_order_against_identical_in_quantity_outstanding_sell_order(double buyOrderLimit,
                                                                                       double expectedTradePrice) {

        given().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.4").build(),
                aBuyLimitOrder().broker("B").quantity("200").price("10.3").build(),
                aSellLimitOrder().broker("C").quantity("100").price("10.7").build(),
                aSellLimitOrder().broker("D").quantity("200").price("10.8").build()
        );

        when().the_following_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("E").quantity("100").price(Double.toString(buyOrderLimit)).build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(
                        new UUID(0, 1),
                        "E",
                        new UUID(0, 2),
                        "C",
                        100,
                        expectedTradePrice,
                        buyOrderLimit,
                        10.7,
                        LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18),
                        "EURUSD"
                )
        ).and().the_market_order_book_is(
                new Book("A", "100", "10.4", "10.8", "200", "D"),
                new Book("B", "200", "10.3", "", "", "")
        );
    }

    @LimitOrder
    @DataProvider({
            "10.4, 10.4",
            "10.3, 10.4",
    })
    @TestTemplate
    void match_a_single_sell_order_against_identical_in_quantity_outstanding_buy_order(double sellOrderLimit,
                                                                                       double expectedTradePrice) {

        given().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.4").build(),
                aBuyLimitOrder().broker("B").quantity("200").price("10.3").build(),
                aSellLimitOrder().broker("C").quantity("100").price("10.7").build(),
                aSellLimitOrder().broker("D").quantity("200").price("10.8").build()
        );

        when().the_following_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("E").quantity("100").price(Double.toString(sellOrderLimit)).build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "A", new UUID(0, 1), "E", 100, expectedTradePrice, 10.4, sellOrderLimit, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("B", "200", "10.3", "10.7", "100", "C"),
                new Book("", "", "", "10.8", "200", "D")
        );
    }

    @LimitOrder
    @Test
    void match_a_buy_order_large_enough_to_clear_the_sell_book() {

        given().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.4").build(),
                aBuyLimitOrder().broker("B").quantity("200").price("10.3").build(),
                aSellLimitOrder().broker("C").quantity("100").price("10.7").build(),
                aSellLimitOrder().broker("D").quantity("200").price("10.8").build()
        );

        when().the_following_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("E").quantity("350").price("10.8").build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "E", new UUID(0, 2), "C", 100, 10.7, 10.8, 10.7, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD"),
                new TradeExecuted(new UUID(0, 3), "E", new UUID(0, 4), "D", 200, 10.8, 10.8, 10.8, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("E", "50", "10.8", "", "", ""),
                new Book("A", "100", "10.4", "", "", ""),
                new Book("B", "200", "10.3", "", "", "")
        );
    }

    @LimitOrder
    @Test
    void match_a_sell_order_large_enough_to_clear_the_buy_book() {

        given().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.4").build(),
                aBuyLimitOrder().broker("B").quantity("200").price("10.3").build(),
                aSellLimitOrder().broker("C").quantity("100").price("10.7").build(),
                aSellLimitOrder().broker("D").quantity("200").price("10.8").build()
        );

        when().the_following_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("E").quantity("350").price("10.3").build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "A", new UUID(0, 3), "E", 100, 10.4, 10.4, 10.3, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD"),
                new TradeExecuted(new UUID(0, 2), "B", new UUID(0, 4), "E", 200, 10.3, 10.3, 10.3, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("", "", "", "10.3", "50", "E"),
                new Book("", "", "", "10.7", "100", "C"),
                new Book("", "", "", "10.8", "200", "D")
        );
    }

    @LimitOrder
    @Test
    void match_a_large_buy_order_partially() {

        given().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.4").build(),
                aBuyLimitOrder().broker("B").quantity("200").price("10.3").build(),
                aSellLimitOrder().broker("C").quantity("100").price("10.7").build(),
                aSellLimitOrder().broker("D").quantity("200").price("10.8").build()
        );

        when().the_following_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("E").quantity("350").price("10.7").build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "E", new UUID(0, 2), "C", 100, 10.7, 10.7, 10.7, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("E", "250", "10.7", "10.8", "200", "D"),
                new Book("A", "100", "10.4", "", "", ""),
                new Book("B", "200", "10.3", "", "", "")
        );
    }

    @LimitOrder
    @Test
    void match_a_large_sell_order_partially() {

        given().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.4").build(),
                aBuyLimitOrder().broker("B").quantity("200").price("10.3").build(),
                aSellLimitOrder().broker("C").quantity("100").price("10.7").build(),
                aSellLimitOrder().broker("D").quantity("200").price("10.8").build()
        );

        when().the_following_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("E").quantity("350").price("10.4").build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "A", new UUID(0, 2), "E", 100, 10.4, 10.4, 10.4, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("B", "200", "10.3", "10.4", "250", "E"),
                new Book("", "", "", "10.7", "100", "C"),
                new Book("", "", "", "10.8", "200", "D")
        );
    }

    @LimitOrder
    @MarketOrder
    @Test
    void match_a_large_buy_market_order_against_multiple_limit_orders() {

        when().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.7").build(),
                aBuyLimitOrder().broker("B").quantity("200").price("10.6").build(),
                aBuyLimitOrder().broker("C").quantity("300").price("10.5").build(),
                Order.OrderRowBuilder.aSellMarketOrder().broker("D").quantity("650").build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "A", new UUID(0, 2), "D", 100, 10.7, 10.7, 0., LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD"),
                new TradeExecuted(new UUID(0, 3), "B", new UUID(0, 4), "D", 200, 10.6, 10.6, 0., LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD"),
                new TradeExecuted(new UUID(0, 5), "C", new UUID(0, 6), "D", 300, 10.5, 10.5, 0., LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("", "", "", "MO", "50", "D")
        );
    }

    @LimitOrder
    @MarketOrder
    @Test
    void match_a_small_buy_market_order_against_limit_orders() {

        when().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.7").build(),
                aBuyLimitOrder().broker("B").quantity("200").price("10.6").build(),
                aBuyLimitOrder().broker("C").quantity("300").price("10.5").build(),
                Order.OrderRowBuilder.aSellMarketOrder().broker("D").quantity("150").build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "A", new UUID(0, 3), "D", 100, 10.7, 10.7, 0., LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD"),
                new TradeExecuted(new UUID(0, 2), "B", new UUID(0, 4), "D", 50, 10.6, 10.6, 0., LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("B", "150", "10.6", "", "", ""),
                new Book("C", "300", "10.5", "", "", "")
        );
    }

    @LimitOrder
    @MarketOrder
    @Test
    void match_a_large_sell_market_order_against_multiple_limit_orders() {

        when().the_following_orders_have_been_submitted_in_this_order(
                aSellLimitOrder().broker("A").quantity("100").price("10.5").build(),
                aSellLimitOrder().broker("B").quantity("200").price("10.6").build(),
                aSellLimitOrder().broker("C").quantity("300").price("10.7").build(),
                aBuyMarketOrder().broker("D").quantity("650").build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "D", new UUID(0, 2), "A", 100, 10.5, 0., 10.5, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD"),
                new TradeExecuted(new UUID(0, 3), "D", new UUID(0, 4), "B", 200, 10.6, 0., 10.6, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD"),
                new TradeExecuted(new UUID(0, 5), "D", new UUID(0, 6), "C", 300, 10.7, 0., 10.7, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("D", "50", "MO", "", "", "")
        );
    }

    @LimitOrder
    @MarketOrder
    @Test
    void match_a_small_sell_market_order_against_limit_orders() {

        when().the_following_orders_have_been_submitted_in_this_order(
                aSellLimitOrder().broker("A").quantity("100").price("10.5").build(),
                aSellLimitOrder().broker("B").quantity("200").price("10.6").build(),
                aSellLimitOrder().broker("C").quantity("300").price("10.7").build(),
                aBuyMarketOrder().broker("D").quantity("150").build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "D", new UUID(0, 2), "A", 100, 10.5, 0., 10.5, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD"),
                new TradeExecuted(new UUID(0, 3), "D", new UUID(0, 4), "B", 50, 10.6, 0., 10.6, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("", "", "", "10.6", "150", "B"),
                new Book("", "", "", "10.7", "300", "C")
        );
    }

    @LimitOrder
    @MarketOrder
    @Test
    void match_a_sell_market_order_against_multiple_limit_orders() {

        when().the_following_orders_have_been_submitted_in_this_order(
                aBuyMarketOrder().broker("A").quantity("300").build(),
                aBuyMarketOrder().broker("B").quantity("100").build(),
                aBuyMarketOrder().broker("C").quantity("150").build(),
                aSellLimitOrder().broker("D").quantity("250").price("10.7").build()
        );

        then().the_following_trades_are_generated(
                new TradeExecuted(new UUID(0, 1), "A", new UUID(0, 2), "D", 250, 10.7, 0., 10.7, LocalDateTime.of(2018, JANUARY, 17, 12, 52, 18), "EURUSD")
        ).and().the_market_order_book_is(
                new Book("A", "50", "MO", "", "", ""),
                new Book("B", "100", "MO", "", "", ""),
                new Book("C", "150", "MO", "", "", "")
        );
    }

    static class OrderMatchingTestSteps extends Stage<OrderMatchingTestSteps> {

        private MatchingEngine matchingEngine;
        private OrderEventListener orderEventListener;
        private ArgumentCaptor<TradeExecuted> tradeCaptor;

        @BeforeStage
        public void before() {
            orderEventListener = mock(OrderEventListener.class);
            matchingEngine = new MatchingEngine(
                    orderEventListener,
                    provideFixedClock(
                            2018,
                            JANUARY,
                            17,
                            12,
                            52,
                            18
                    )
            );
            tradeCaptor = forClass(TradeExecuted.class);
        }

        void the_following_orders_are_submitted_in_this_order(@Table(columnTitles = {"Broker", "Side", "Qty", "Price"}, excludeFields = {"type", "symbol", "openQuantity", "executedQuantity"}) Order... orders) {
            submitOrders(orders);
        }

        void the_following_orders_have_been_submitted_in_this_order(@Table(columnTitles = {"Broker", "Side", "Qty", "Price"}, excludeFields = {"type", "symbol", "openQuantity", "executedQuantity"}) Order... orders) {
            submitOrders(orders);
        }

        private void submitOrders(Order... orders) {

            Arrays.stream(orders).map(order -> {
                final Order orderRow = order;
                return orderRow.type.accept(new OrderTypeVisitor<SubmitOrder>() {
                    @Override
                    public SubmitOrder visitMarket() {
                        return aSubmitMarketOrder(
                                "EURUSD",
                                orderRow.broker,
                                parseInt(orderRow.quantity),
                                orderRow.side
                        );
                    }

                    @Override
                    public SubmitOrder visitLimit() {
                        return aSubmitLimitOrder(
                                "EURUSD",
                                orderRow.broker,
                                parseInt(orderRow.quantity),
                                orderRow.side,
                                parseDouble(orderRow.price)
                        );
                    }
                });
            }).forEach(matchingEngine::submitOrder);
        }

        <A, B, R> Stream<R> createOrderBook(
                Stream<A> streamA, Stream<B> streamB, BiFunction<? super A, ? super B, R> function) {
            Spliterator<A> splitrA = streamA.spliterator();
            Spliterator<B> splitrB = streamB.spliterator();
            int characteristics =
                    splitrA.characteristics()
                            & splitrB.characteristics()
                            & (SIZED | ORDERED);
            Iterator<A> itrA = Spliterators.iterator(splitrA);
            Iterator<B> itrB = Spliterators.iterator(splitrB);
            return stream(
                    new AbstractSpliterator<>(
                            Math.min(splitrA.estimateSize(), splitrB.estimateSize()), characteristics) {
                        @Override
                        public boolean tryAdvance(Consumer<? super R> action) {
                            if (itrA.hasNext() && itrB.hasNext()) {
                                action.accept(function.apply(itrA.next(), itrB.next()));
                                return true;
                            } else if (itrA.hasNext()) {
                                action.accept(function.apply(itrA.next(), null));
                                return true;
                            } else if (itrB.hasNext()) {
                                action.accept(function.apply(null, itrB.next()));
                                return true;
                            }
                            return false;
                        }
                    }, false);
        }

        OrderMatchingTestSteps the_following_trades_are_generated(@Table(columnTitles = {"Buying broker", "Selling broker", "Qty", "Price"}, excludeFields = {"buyingId", "sellingId", "buyingLimit", "sellingLimit", "time", "symbol"}) TradeExecuted... tradeExecuteds) {
            verify(orderEventListener, times(tradeExecuteds.length)).onTradeExecuted(tradeCaptor.capture());
            final List<TradeExecuted> tradeExecutedList = tradeCaptor.getAllValues();
            assertThat(tradeExecutedList).usingElementComparatorIgnoringFields("buyingId", "sellingId").containsExactly(tradeExecuteds);
            return self();
        }

        void the_market_order_book_is(@Table(columnTitles = {
                "Broker", "Qty", "Price", "Price", "Qty", "Broker"}) Book... orderBooks) {

            final List<Book> books = createOrderBook(
                    matchingEngine.getBuyOrderBook().stream(),
                    matchingEngine.getSellOrderBook().stream(),
                    (buyOrder, sellOrder) -> new Book(
                            ofNullable(buyOrder).map(order -> order.broker).orElse(""),
                            ofNullable(buyOrder).map(order -> String.valueOf(order.getOpenQuantity())).orElse(""),
                            ofNullable(buyOrder).map(order -> order.accept(new org.trading.matching.engine.domain.Order.OrderVisitor<String>() {
                                @Override
                                public String visitMarketOrder(org.trading.matching.engine.domain.MarketOrder marketOrder) {
                                    return "MO";
                                }

                                @Override
                                public String visitLimitOrder(org.trading.matching.engine.domain.LimitOrder limitOrder) {
                                    return String.valueOf(limitOrder.limit);
                                }
                            })).orElse(""),
                            ofNullable(sellOrder).map(order -> order.accept(new org.trading.matching.engine.domain.Order.OrderVisitor<String>() {
                                @Override
                                public String visitMarketOrder(org.trading.matching.engine.domain.MarketOrder marketOrder) {
                                    return "MO";
                                }

                                @Override
                                public String visitLimitOrder(org.trading.matching.engine.domain.LimitOrder limitOrder) {
                                    return String.valueOf(limitOrder.limit);
                                }
                            })).orElse(""),
                            ofNullable(sellOrder).map(order -> String.valueOf(order.getOpenQuantity())).orElse(""),
                            ofNullable(sellOrder).map(order -> order.broker).orElse("")
                    )).collect(toList());

            assertThat(books).containsExactly(orderBooks);
        }

        Clock provideFixedClock(int year, Month month, int dayOfMonth, int hour, int minute, int second) {
            return fixed(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second)
                    .toInstant(UTC), ZoneId.of("UTC"));
        }
    }
}
