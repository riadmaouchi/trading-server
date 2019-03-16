package org.trading.trade.execution.esp.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.annotation.Table;
import com.tngtech.jgiven.junit5.SimpleScenarioTest;
import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.trading.api.event.LimitOrderAccepted;
import org.trading.api.message.Side;
import org.trading.trade.execution.bdd.model.Order;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;
import org.trading.trade.execution.esp.domain.ExecutionRejected;
import org.trading.trade.execution.esp.domain.LastLook;
import org.trading.trade.execution.esp.domain.TradeListener;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.trading.trade.execution.TradeBuilder.aTrade;
import static org.trading.trade.execution.bdd.model.OrderBuilder.aBuyLimitOrder;
import static org.trading.trade.execution.bdd.model.OrderBuilder.aSellLimitOrder;

@ExtendWith(DataProviderExtension.class)
class ExecutionTest extends SimpleScenarioTest<ExecutionTest.ExecutionTestSteps> {

    @DataProvider({
            "1.208",
            "1.22008",
            "1.19592"
    })
    @TestTemplate
    void accept_buy_trade_request_when_the_refreshed_price_has_not_moved_in_either_direction_by_more_than_a_defined_price_tolerance(double price) {

        given().the_price_tolerance_for_execution_is(0.01)
                .and().the_following_orders_have_been_submitted_in_this_order(
                aSellLimitOrder().quantity("200000").price("1.24").build(),
                aSellLimitOrder().quantity("500000").price("1.20").build(),
                aSellLimitOrder().quantity("300000").price("1.22").build(),
                aSellLimitOrder().quantity("200000").price("1.21").build()
        );

        when().a_buy_trade_execution_is_submit_for_a_quantity_of_$_at(1000000, price);

        then().the_execution_is_accepted_at(1.208);
    }

    @DataProvider({
            "1.208",
            "1.22008",
            "1.19592"
    })
    @TestTemplate
    void accept_sell_trade_request_when_the_refreshed_price_has_not_moved_in_either_direction_by_more_than_a_defined_price_tolerance(double price) {

        given().the_price_tolerance_for_execution_is(0.01)
                .and().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().quantity("500000").price("1.20").build(),
                aBuyLimitOrder().quantity("200000").price("1.21").build(),
                aBuyLimitOrder().quantity("200000").price("1.19").build(),
                aBuyLimitOrder().quantity("300000").price("1.22").build()
        );

        when().a_sell_trade_execution_is_submit_for_a_quantity_of_$_at(1000000, price);

        then().the_execution_is_accepted_at(1.208);
    }


    @DataProvider({
            "1.22009",
            "1.19590"
    })
    @TestTemplate
    void reject_buy_trade_request_when_the_refreshed_price_has_moved_in_either_direction_by_more_than_a_defined_price_tolerance(double price) {

        given().the_price_tolerance_for_execution_is(0.01)
                .and().the_following_orders_have_been_submitted_in_this_order(
                aSellLimitOrder().quantity("200000").price("1.24").build(),
                aSellLimitOrder().quantity("500000").price("1.20").build(),
                aSellLimitOrder().quantity("300000").price("1.22").build(),
                aSellLimitOrder().quantity("200000").price("1.21").build()
        );

        when().a_buy_trade_execution_is_submit_for_a_quantity_of_$_at(1000000, price);

        then().the_execution_is_rejected();
    }

    @DataProvider({
            "1.22009",
            "1.19590"
    })
    @TestTemplate
    void reject_sell_trade_request_when_the_refreshed_price_has_moved_in_either_direction_by_more_than_a_defined_price_tolerance(double price) {

        given().the_price_tolerance_for_execution_is(0.01)
                .and().the_following_orders_have_been_submitted_in_this_order(
                aBuyLimitOrder().quantity("500000").price("1.20").build(),
                aBuyLimitOrder().quantity("200000").price("1.21").build(),
                aBuyLimitOrder().quantity("200000").price("1.19").build(),
                aBuyLimitOrder().quantity("300000").price("1.22").build()
        );

        when().a_sell_trade_execution_is_submit_for_a_quantity_of_$_at(1000000, price);

        then().the_execution_is_rejected();
    }


    @Test
    void reject_trade_request_when_liquidity_is_insufficient() {

        given().the_price_tolerance_for_execution_is(0.01)
                .and().the_following_orders_have_been_submitted_in_this_order(
                aSellLimitOrder().quantity("200000").price("1.24").build()
        );

        when().a_buy_trade_execution_is_submit_for_a_quantity_of_$_at(1000000, 1.208);

        then().the_execution_is_rejected();
    }

    static class ExecutionTestSteps extends Stage<ExecutionTestSteps> {

        private LastLook lastLook;

        private TradeListener tradeListener;
        private ArgumentCaptor<ExecutionAccepted> executionAcceptedArgumentCaptor;
        private ArgumentCaptor<ExecutionRejected> executionRejectedArgumentCaptor;

        @BeforeStage
        public void before() {
            tradeListener = mock(TradeListener.class);
            executionAcceptedArgumentCaptor = forClass(ExecutionAccepted.class);
            executionRejectedArgumentCaptor = forClass(ExecutionRejected.class);
        }

        ExecutionTestSteps the_price_tolerance_for_execution_is(@Quoted double tolerance) {
            lastLook = new LastLook(tradeListener, tolerance);
            return self();
        }

        void the_following_orders_have_been_submitted_in_this_order(@Table(columnTitles = {"Side", "Qty", "Price"},
                excludeFields = {"broker", "symbol"}) Order... orders) {
            IntStream.range(0, orders.length)
                    .mapToObj(id -> {
                        final Order order = orders[id];
                        return new LimitOrderAccepted(
                                new UUID(0, id),
                                LocalDateTime.now(),
                                order.broker,
                                parseInt(order.quantity),
                                order.side,
                                parseDouble(order.price),
                                order.symbol
                        );
                    }).forEach(lastLook::onLimitOrderPlaced);
        }

        void a_buy_trade_execution_is_submit_for_a_quantity_of_$_at(@Quoted int quantity, @Quoted double price) {
            a_trade_execution_is_submit(quantity, price, Side.BUY);
        }

        private void a_trade_execution_is_submit(int quantity, double price, Side side) {
            lastLook.requestExecution(aTrade()
                    .withSide(side)
                    .withQuantity(quantity)
                    .withPrice(price)
                    .build());
        }

        void a_sell_trade_execution_is_submit_for_a_quantity_of_$_at(@Quoted int quantity, @Quoted double price) {
            a_trade_execution_is_submit(quantity, price, Side.SELL);
        }

        void the_execution_is_accepted_at(@Quoted double price) {
            verify(tradeListener).onExecutionAccepted(executionAcceptedArgumentCaptor.capture());
            verify(tradeListener, never()).onExecutionRejected(any(ExecutionRejected.class));
            ExecutionAccepted executionAccepted = executionAcceptedArgumentCaptor.getValue();
            assertThat(executionAccepted.price).isEqualTo(price);
        }

        void the_execution_is_rejected() {
            verify(tradeListener, never()).onExecutionAccepted(any(ExecutionAccepted.class));
            verify(tradeListener).onExecutionRejected(any(ExecutionRejected.class));
        }
    }
}