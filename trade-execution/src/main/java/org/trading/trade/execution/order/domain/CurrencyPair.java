package org.trading.trade.execution.order.domain;

import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import org.trading.api.message.Side.SideVisitor;
import org.trading.trade.execution.order.event.LastTradeExecuted;
import org.trading.trade.execution.order.event.OrderLevelUpdated;

import java.util.Optional;
import java.util.function.BiFunction;

public class CurrencyPair {

    private final Double2ObjectOpenHashMap<OrderLevelUpdated> buyOrders = new Double2ObjectOpenHashMap<>();
    private final Double2ObjectOpenHashMap<OrderLevelUpdated> sellOrders = new Double2ObjectOpenHashMap<>();
    private LastTradeExecuted lastTrade;
    private double open;
    private double high;
    private double low;
    private double close;


    public CurrencyPair(double open) {
        this.open = open;
        high = open;
        low = open;
        close = open;
    }

    public OrderLevelUpdated placeOrder(OrderLevelUpdated order) {
        return order.side.accept(new SideVisitor<>() {
            private final int quantity = order.quantity;

            @Override
            public OrderLevelUpdated visitBuy() {
                return buyOrders.merge(order.price, order, increaseSize());
            }

            @Override
            public OrderLevelUpdated visitSell() {
                return sellOrders.merge(order.price, order, increaseSize());
            }
        });
    }

    public OrderLevelUpdated executeOrder(OrderLevelUpdated order) {

        return order.side.accept(new SideVisitor<>() {

            @Override
            public OrderLevelUpdated visitBuy() {
                OrderLevelUpdated orderLevelUpdated = buyOrders.merge(order.price, order, decreaseSize());
                buyOrders.double2ObjectEntrySet()
                        .removeIf(order -> order.getDoubleKey() == 0d);
                return orderLevelUpdated;
            }

            @Override
            public OrderLevelUpdated visitSell() {
                OrderLevelUpdated orderLevelUpdated = sellOrders.merge(order.price, order, decreaseSize());
                sellOrders.double2ObjectEntrySet()
                        .removeIf(order -> order.getDoubleKey() == 0d);
                return orderLevelUpdated;
            }
        });
    }

    private BiFunction<OrderLevelUpdated, OrderLevelUpdated, OrderLevelUpdated> increaseSize() {
        return (previous, last) -> new OrderLevelUpdated(
                previous.symbol,
                previous.side,
                previous.quantity + last.quantity,
                previous.price
        );
    }

    private BiFunction<OrderLevelUpdated, OrderLevelUpdated, OrderLevelUpdated> decreaseSize() {
        return (previous, last) -> new OrderLevelUpdated(
                previous.symbol,
                previous.side,
                previous.quantity - last.quantity,
                previous.price
        );
    }

    public LastTradeExecuted update(LastTradeExecuted lastTradeExecuted) {
        double price = lastTradeExecuted.lastPrice;
        high = high < price ? price : high;
        low = low > price ? price : low;
        close = price;
        LastTradeExecuted tradeExecuted = new LastTradeExecuted(
                lastTradeExecuted.symbol,
                lastTradeExecuted.lastPrice,
                lastTradeExecuted.lastQuantity,
                lastTradeExecuted.time,
                open,
                high,
                low,
                close
        );
        this.lastTrade = tradeExecuted;
        return tradeExecuted;
    }

    public CurrencyPair updateOHLC() {
        Optional.ofNullable(lastTrade).ifPresent(lastTradeExecuted -> {
            double price = lastTrade.lastPrice;
            open = price;
            close = price;
            high = price;
            low = price;
        });
        return this;
    }

    public Double2ObjectOpenHashMap<OrderLevelUpdated> getBuyOrders() {
        return new Double2ObjectOpenHashMap<>(buyOrders);
    }

    public Double2ObjectOpenHashMap<OrderLevelUpdated> getSellOrders() {
        return new Double2ObjectOpenHashMap<>(sellOrders);
    }

    public Optional<LastTradeExecuted> getLastTrade() {
        return Optional.ofNullable(lastTrade);
    }
}
