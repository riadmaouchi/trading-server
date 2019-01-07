package org.trading.matching.engine.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.annotation.Table;
import com.tngtech.jgiven.junit5.SimpleScenarioTest;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.api.command.OrderType.OrderTypeVisitor;
import org.trading.api.command.Side;
import org.trading.matching.engine.bdd.model.Order;
import org.trading.matching.engine.bdd.tag.LimitOrder;
import org.trading.matching.engine.bdd.tag.MarketOrder;
import org.trading.matching.engine.bdd.tag.OrderBook;

import java.util.List;
import java.util.UUID;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.command.Side.BUY;
import static org.trading.api.command.Side.SELL;
import static org.trading.matching.engine.bdd.model.Order.OrderRowBuilder.*;

@OrderBook
@ExtendWith(DataProviderExtension.class)
class OrderBookTest extends SimpleScenarioTest<OrderBookTest.OrderBookTestSteps> {

    @LimitOrder
    @Test
    void submit_a_buy_limit_order() {

        when().the_following_buy_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.5").build()
        );

        then().the_buy_order_book_looks_like(
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );
    }

    @LimitOrder
    @Test
    void submit_two_buy_limit_orders_with_the_more_aggressive_order_first() {

        when().the_following_buy_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.5").build(),
                aBuyLimitOrder().broker("B").quantity("100").price("10.4").build()
        );

        then().the_buy_order_book_looks_like(
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build(),
                aBuyLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.4").build()
        );
    }

    @LimitOrder
    @Test
    void submit_two_buy_limit_orders_with_less_aggressive_order_first() {

        when().the_following_buy_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("B").quantity("100").price("10.4").build(),
                aBuyLimitOrder().broker("A").quantity("100").price("10.5").build()
        );

        then().the_buy_order_book_looks_like(
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build(),
                aBuyLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.4").build()
        );
    }

    @LimitOrder
    @Test
    void submit_a_sell_limit_order() {

        when().the_following_sell_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("A").quantity("100").price("10.5").build()
        );

        then().the_sell_order_book_looks_like(
                aSellLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );
    }

    @LimitOrder
    @Test
    void submit_two_sell_limit_orders_with_the_more_aggressive_order_first() {

        when().the_following_sell_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("A").quantity("100").price("10.4").build(),
                aSellLimitOrder().broker("B").quantity("100").price("10.5").build()
        );

        then().the_sell_order_book_looks_like(
                aSellLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.4").build(),
                aSellLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );
    }

    @LimitOrder
    @Test
    void submit_two_sell_limit_orders_with_less_aggressive_order_first() {

        when().the_following_sell_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("B").quantity("100").price("10.5").build(),
                aSellLimitOrder().broker("A").quantity("100").price("10.4").build()
        );

        then().the_sell_order_book_looks_like(
                aSellLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.4").build(),
                aSellLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );
    }

    @LimitOrder
    @Test
    void submit_two_limit_orders_to_the_buy_order_book_with_the_same_price_limit() {

        when().the_following_buy_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.4").build(),
                aBuyLimitOrder().broker("B").quantity("100").price("10.4").build()
        );

        then().the_buy_order_book_looks_like(
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.4").build(),
                aBuyLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.4").build()
        );
    }

    @Test
    void submit_two_limit_orders_to_the_sell_order_book_with_the_same_price_limit() {

        when().the_following_sell_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("A").quantity("100").price("10.4").build(),
                aSellLimitOrder().broker("B").quantity("100").price("10.4").build()
        );

        then().the_sell_order_book_looks_like(
                aSellLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.4").build(),
                aSellLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.4").build()
        );
    }

    @LimitOrder
    @Test
    void decrease_top_outstanding_buy_order_partially_and_then_fill_it_completely() {

        when().the_following_buy_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.5").build(),
                aBuyLimitOrder().broker("B").quantity("100").price("10.5").build()
        );

        then().the_buy_order_book_looks_like(
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build(),
                aBuyLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_top_order_of_the_$_order_book_is_filled_by_(BUY, 20);

        then().the_buy_order_book_looks_like(
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("80").executedQuantity("20").price("10.5").build(),
                aBuyLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_top_order_of_the_$_order_book_is_filled_by_(BUY, 80);

        then().the_buy_order_book_looks_like(
                aBuyLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );
    }

    @LimitOrder
    @Test
    void decrease_top_outstanding_sell_order_partially_and_then_fill_it_completely() {

        when().the_following_sell_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("A").quantity("100").price("10.5").build(),
                aSellLimitOrder().broker("B").quantity("100").price("10.5").build()
        );

        then().the_sell_order_book_looks_like(
                aSellLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build(),
                aSellLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_top_order_of_the_$_order_book_is_filled_by_(SELL, 20);

        then().the_sell_order_book_looks_like(
                aSellLimitOrder().broker("A").quantity("100").openQuantity("80").executedQuantity("20").price("10.5").build(),
                aSellLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_top_order_of_the_$_order_book_is_filled_by_(SELL, 80);

        then().the_sell_order_book_looks_like(
                aSellLimitOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );
    }

    @LimitOrder
    @MarketOrder
    @Test
    void submit_market_buy_order_with_price_and_time_priority_over_limit_order() {

        when().the_following_buy_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("A").quantity("100").price("10.5").build()
        );

        then().the_buy_order_book_looks_like(
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_following_buy_orders_are_submitted_in_this_order(
                aBuyMarketOrder().broker("B").quantity("100").build()
        );

        then().the_buy_order_book_looks_like(
                aBuyMarketOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").build(),
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_following_buy_orders_are_submitted_in_this_order(
                aBuyLimitOrder().broker("C").quantity("100").price("10.5").build()
        );

        then().the_buy_order_book_looks_like(
                aBuyMarketOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").build(),
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build(),
                aBuyLimitOrder().broker("C").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_following_buy_orders_are_submitted_in_this_order(
                aBuyMarketOrder().broker("D").quantity("100").build()
        );

        then().the_buy_order_book_looks_like(
                aBuyMarketOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").build(),
                aBuyMarketOrder().broker("D").quantity("100").openQuantity("100").executedQuantity("0").build(),
                aBuyLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build(),
                aBuyLimitOrder().broker("C").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

    }

    @LimitOrder
    @MarketOrder
    @Test
    void submit_market_sell_order_with_price_and_time_priority_over_limit_order() {

        when().the_following_sell_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("A").quantity("100").price("10.5").build()
        );

        then().the_sell_order_book_looks_like(
                aSellLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_following_sell_orders_are_submitted_in_this_order(
                aSellMarketOrder().broker("B").quantity("100").build()
        );

        then().the_sell_order_book_looks_like(
                aSellMarketOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").build(),
                aSellLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_following_sell_orders_are_submitted_in_this_order(
                aSellLimitOrder().broker("C").quantity("100").price("10.5").build()
        );

        then().the_sell_order_book_looks_like(
                aSellMarketOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").build(),
                aSellLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build(),
                aSellLimitOrder().broker("C").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );

        when().the_following_sell_orders_are_submitted_in_this_order(
                aSellMarketOrder().broker("D").quantity("100").openQuantity("100").executedQuantity("0").build()
        );

        then().the_sell_order_book_looks_like(
                aSellMarketOrder().broker("B").quantity("100").openQuantity("100").executedQuantity("0").build(),
                aSellMarketOrder().broker("D").quantity("100").openQuantity("100").executedQuantity("0").build(),
                aSellLimitOrder().broker("A").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build(),
                aSellLimitOrder().broker("C").quantity("100").openQuantity("100").executedQuantity("0").price("10.5").build()
        );
    }

    static class OrderBookTestSteps extends Stage<OrderBookTestSteps> {

        private org.trading.matching.engine.domain.OrderBook buyOrderBook;
        private org.trading.matching.engine.domain.OrderBook sellOrderBook;

        @BeforeStage
        public void before() {
            buyOrderBook = new org.trading.matching.engine.domain.OrderBook(BUY);
            sellOrderBook = new org.trading.matching.engine.domain.OrderBook(SELL);
        }

        void the_following_buy_orders_are_submitted_in_this_order(@Table(columnTitles = {"Symbol", "Broker", "Side", "Qty", "Price", "Type"}, excludeFields = {"openQuantity", "executedQuantity"}) Order... orders) {
            submitOrders(orders);
        }

        void the_following_sell_orders_are_submitted_in_this_order(@Table(columnTitles = {"Symbol", "Broker", "Side", "Qty", "Price", "Type"}, excludeFields = {"openQuantity", "executedQuantity"}) Order... orders) {
            submitOrders(orders);
        }

        void submitOrders(Order... orders) {
            stream(orders).map(o -> o.type.accept(new OrderTypeVisitor<org.trading.matching.engine.domain.Order>() {
                @Override
                public org.trading.matching.engine.domain.Order visitMarket() {
                    return new org.trading.matching.engine.domain.MarketOrder(
                            new UUID(0, 1),
                            o.symbol,
                            o.broker,
                            parseInt(o.quantity),
                            o.side,
                            now()
                    );
                }

                @Override
                public org.trading.matching.engine.domain.Order visitLimit() {
                    return new org.trading.matching.engine.domain.LimitOrder(
                            new UUID(0, 1),
                            o.symbol,
                            o.broker,
                            parseInt(o.quantity),
                            o.side,
                            parseDouble(o.price),
                            now()
                    );
                }
            })).forEach(order -> order.side.accept(new Side.SideVisitor<Void>() {
                @Override
                public Void visitBuy() {
                    buyOrderBook.place(order);
                    return null;
                }

                @Override
                public Void visitSell() {
                    sellOrderBook.place(order);
                    return null;
                }
            }));
        }

        void the_buy_order_book_looks_like(@Table(columnTitles = {"Symbol", "Broker", "Side", "Qty", "OpenQty", "ExecQty", "Price", "Type"}) Order... orders) {
            the_buy_order_book_looks_like(buyOrderBook.orders(), orders);
        }

        void the_sell_order_book_looks_like(@Table(columnTitles = {"Symbol", "Broker", "Side", "Qty", "OpenQty", "ExecQty", "Price", "Type"}) Order... orders) {
            the_buy_order_book_looks_like(sellOrderBook.orders(), orders);
        }

        void the_buy_order_book_looks_like(List<org.trading.matching.engine.domain.Order> ordersList, Order... orders) {
            assertThat(ordersList).extracting("symbol")
                    .containsExactly(stream(orders).map(o -> o.symbol).toArray());
            assertThat(ordersList).extracting("broker")
                    .containsExactly(stream(orders).map(o -> o.broker).toArray());
            assertThat(ordersList).extracting("side")
                    .containsExactly(stream(orders).map(o -> o.side).toArray());
            assertThat(ordersList).extracting("quantity")
                    .containsExactly(stream(orders).map(o -> parseInt(o.quantity)).toArray());
            assertThat(ordersList).extracting("openQuantity")
                    .containsExactly(stream(orders).map(o -> parseInt(o.openQuantity)).toArray());
            assertThat(ordersList).extracting("executedQuantity")
                    .containsExactly(stream(orders).map(o -> parseInt(o.executedQuantity)).toArray());

            assertThat(ordersList).filteredOn(order -> order instanceof org.trading.matching.engine.domain.LimitOrder)
                    .extracting("limit")
                    .containsExactly(stream(orders).filter(orderRow -> orderRow.type.accept(new OrderTypeVisitor<>() {
                        @Override
                        public Boolean visitMarket() {
                            return false;
                        }

                        @Override
                        public Boolean visitLimit() {
                            return true;
                        }
                    })).map(orderRow -> parseDouble(orderRow.price)).toArray());
        }

        void the_top_order_of_the_$_order_book_is_filled_by_(@Quoted Side side, @Quoted int quantity) {
            side.accept(new Side.SideVisitor<Void>() {
                @Override
                public Void visitBuy() {
                    buyOrderBook.decreaseTopBy(quantity);
                    return null;
                }

                @Override
                public Void visitSell() {
                    sellOrderBook.decreaseTopBy(quantity);
                    return null;
                }
            });
        }
    }
}