package org.trading.trade.execution.esp.domain;

import it.unimi.dsi.fastutil.doubles.Double2IntAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.trading.api.command.Side;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.TradeExecuted;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class LastLook {
    private final TradeListener tradeListener;
    private final double tolerance;
    private final Set<ExecutionAccepted> acceptedExecutions = new HashSet<>();
    private final Set<ExecutionRejected> rejectedExecutions = new HashSet<>();
    private final Map<String, MarketDepth> depths = new HashMap<>();

    public LastLook(TradeListener priceListener, double tolerance) {
        this.tradeListener = priceListener;
        this.tolerance = tolerance;
    }

    public void requestExecution(Trade trade) {
        MarketDepth marketDepth = depths.get(trade.symbol);
        Double2IntAVLTreeMap orders = trade.side.accept(new Side.SideVisitor<>() {
            @Override
            public Double2IntAVLTreeMap visitBuy() {
                return marketDepth.sellOrders;
            }

            @Override
            public Double2IntAVLTreeMap visitSell() {
                return marketDepth.buyOrders;
            }
        });
        int liquidity = orders.values().stream()
                .mapToInt(Number::intValue)
                .sum();

        if (liquidity < trade.quantity) {
            ExecutionRejected executionRejected = new ExecutionRejected(
                    LocalDateTime.now(),
                    trade.id,
                    trade.symbol,
                    trade.side,
                    trade.price,
                    trade.quantity,
                    trade.broker,
                    format("Insufficient liquidity : %s < %s", liquidity, trade.quantity)
            );
            tradeListener.onExecutionRejected(executionRejected);
            rejectedExecutions.add(executionRejected);
            return;
        }

        double refreshPrice = computePrice(trade.quantity, orders);
        double delta = refreshPrice * tolerance;
        boolean isAccepted = trade.price >= refreshPrice - delta && trade.price <= refreshPrice + delta;

        if (isAccepted) {
            ExecutionAccepted executionAccepted = new ExecutionAccepted(
                    LocalDateTime.now(),
                    trade.id,
                    trade.symbol,
                    trade.side,
                    refreshPrice,
                    trade.quantity,
                    trade.broker
            );
            tradeListener.onExecutionAccepted(executionAccepted);
            acceptedExecutions.add(executionAccepted);
        } else {
            ExecutionRejected executionRejected = new ExecutionRejected(
                    LocalDateTime.now(),
                    trade.id,
                    trade.symbol,
                    trade.side,
                    refreshPrice,
                    trade.quantity,
                    trade.broker,
                    format("Last look failed :  %s <> %s", trade.price, refreshPrice));
            tradeListener.onExecutionRejected(executionRejected);
            rejectedExecutions.add(executionRejected);
        }
    }

    private double computePrice(double requestedQuantity, Double2IntMap orderBook) {
        final ObjectIterator<Double2IntMap.Entry> it = orderBook.double2IntEntrySet().iterator();
        double totalVolume = 0.;
        double averagePrice = 0.;
        while (it.hasNext() && (totalVolume < requestedQuantity)) {
            final Double2IntMap.Entry order = it.next();
            final double liquidity = order.getIntValue();
            final double price = order.getDoubleKey();
            double quantity = requestedQuantity - totalVolume;
            totalVolume += liquidity;
            averagePrice += price * (totalVolume < requestedQuantity ? liquidity : quantity) / requestedQuantity;
        }
        double pow = Math.pow(10, 5);
        return Math.round(averagePrice * pow) / pow;
    }

    public void onLimitOrderPlaced(LimitOrderPlaced limitOrderPlaced) {

        depths.computeIfAbsent(limitOrderPlaced.symbol, symbol -> new MarketDepth())
                .orders(limitOrderPlaced.side)
                .merge(
                        limitOrderPlaced.price,
                        limitOrderPlaced.quantity,
                        (initialQuantity, quantity) -> initialQuantity + quantity
                );
    }

    public void onTradeExecuted(TradeExecuted tradeExecuted) {

        depths.get(tradeExecuted.symbol).buyOrders.computeIfPresent(
                tradeExecuted.buyingLimit,
                (price, quantity) -> quantity - tradeExecuted.quantity
        );

        depths.get(tradeExecuted.symbol).sellOrders.computeIfPresent(
                tradeExecuted.sellingLimit,
                (price, quantity) -> quantity - tradeExecuted.quantity
        );
    }

    public void reportExecutions() {
        acceptedExecutions.forEach(tradeListener::onExecutionAccepted);
        rejectedExecutions.forEach(tradeListener::onExecutionRejected);
    }

    private final class MarketDepth {
        private final Double2IntAVLTreeMap buyOrders = new Double2IntAVLTreeMap(Comparator.reverseOrder());
        private final Double2IntAVLTreeMap sellOrders = new Double2IntAVLTreeMap(Double::compareTo);

        Double2IntAVLTreeMap orders(Side side) {
            return side.accept(new Side.SideVisitor<>() {
                @Override
                public Double2IntAVLTreeMap visitBuy() {
                    return buyOrders;
                }

                @Override
                public Double2IntAVLTreeMap visitSell() {
                    return sellOrders;
                }
            });
        }
    }
}
