package org.trading.matching.engine.bdd.model;

import com.google.common.base.MoreObjects;
import org.trading.api.message.OrderType;
import org.trading.api.message.Side;

import java.util.Objects;

import static org.trading.api.message.OrderType.LIMIT;
import static org.trading.api.message.OrderType.MARKET;
import static org.trading.api.message.Side.BUY;
import static org.trading.api.message.Side.SELL;

public final class Order {
    public final String symbol;
    public final String broker;
    public final Side side;
    public final String quantity;
    public final String openQuantity;
    public final String executedQuantity;
    public final String price;
    public final OrderType type;

    private Order(String symbol,
                  String broker,
                  Side side,
                  String quantity,
                  String openQuantity,
                  String executedQuantity,
                  String price,
                  OrderType type) {
        this.symbol = symbol;
        this.broker = broker;
        this.quantity = quantity;
        this.side = side;
        this.openQuantity = openQuantity;
        this.executedQuantity = executedQuantity;
        this.price = price;
        this.type = type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Order)) {
            return false;
        }
        Order that = (Order) other;
        return Objects.equals(symbol, that.symbol) &&
                Objects.equals(broker, that.broker) &&
                side == that.side &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(openQuantity, that.openQuantity) &&
                Objects.equals(executedQuantity, that.executedQuantity) &&
                Objects.equals(price, that.price) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, broker, side, quantity, openQuantity, executedQuantity, price, type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("symbol", symbol)
                .add("broker", broker)
                .add("side", side)
                .add("amount", quantity)
                .add("openQuantity", openQuantity)
                .add("executedQuantity", executedQuantity)
                .add("price", price)
                .add("type", type)
                .toString();
    }

    public static final class OrderRowBuilder {
        private String symbol = "EURUSD";
        private String broker = "Broker";
        private Side side = BUY;
        private String quantity = "10";
        private String openQuantity = "10";
        private String executedQuantity = "0";
        private String price = "1.34";
        private OrderType orderType = MARKET;

        private OrderRowBuilder() {
        }

        private static OrderRowBuilder anOrderRow() {
            return new OrderRowBuilder();
        }

        private static OrderRowBuilder aMarketOrder() {
            return anOrderRow().orderType(MARKET).price("MO");
        }

        public static OrderRowBuilder aBuyMarketOrder() {
            return aMarketOrder().side(BUY);
        }

        public static OrderRowBuilder aSellMarketOrder() {
            return aMarketOrder().side(SELL);
        }

        private static OrderRowBuilder aLimitOrder() {
            return anOrderRow().orderType(LIMIT);
        }

        public static OrderRowBuilder aBuyLimitOrder() {
            return aLimitOrder().side(BUY);
        }

        public static OrderRowBuilder aSellLimitOrder() {
            return aLimitOrder().side(SELL);
        }

        public Order build() {
            return new Order(
                    symbol,
                    broker,
                    side,
                    quantity,
                    openQuantity,
                    executedQuantity,
                    price,
                    orderType
            );
        }

        public OrderRowBuilder broker(final String broker) {
            this.broker = broker;
            return this;
        }

        private OrderRowBuilder side(final Side side) {
            this.side = side;
            return this;
        }

        public OrderRowBuilder quantity(final String quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderRowBuilder openQuantity(final String openQuantity) {
            this.openQuantity = openQuantity;
            return this;
        }

        public OrderRowBuilder executedQuantity(final String executedQuantity) {
            this.executedQuantity = executedQuantity;
            return this;
        }

        public OrderRowBuilder price(final String price) {
            this.price = price;
            return this;
        }

        private OrderRowBuilder orderType(final OrderType orderType) {
            this.orderType = orderType;
            return this;
        }
    }

}
