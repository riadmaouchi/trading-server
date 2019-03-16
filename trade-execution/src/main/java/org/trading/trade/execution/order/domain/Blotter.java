package org.trading.trade.execution.order.domain;

import org.trading.api.event.LimitOrderAccepted;
import org.trading.api.event.MarketOrderAccepted;
import org.trading.api.event.MarketOrderRejected;
import org.trading.api.event.TradeExecuted;
import org.trading.api.service.OrderEventListener;
import org.trading.trade.execution.order.event.OrderUpdated;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.trading.api.message.Side.BUY;
import static org.trading.api.message.Side.SELL;
import static org.trading.trade.execution.order.event.OrderUpdated.Status.CANCELLED;
import static org.trading.trade.execution.order.event.OrderUpdated.Status.DONE;
import static org.trading.trade.execution.order.event.OrderUpdated.Status.SUBMITTING;
import static org.trading.trade.execution.order.event.OrderUpdated.Status.WORKING;
import static org.trading.trade.execution.order.event.OrderUpdated.Type.LIMIT;
import static org.trading.trade.execution.order.event.OrderUpdated.Type.MARKET;

public class Blotter implements OrderEventListener {
    private final Map<UUID, OrderUpdated> orders = new HashMap<>();
    private final OrderListener orderListener;

    public Blotter(OrderListener orderListener) {
        this.orderListener = orderListener;
    }

    @Override
    public void onMarketOrderPlaced(MarketOrderAccepted marketOrderPlaced) {
        OrderUpdated orderUpdated = new OrderUpdated(
                marketOrderPlaced.id,
                marketOrderPlaced.time,
                marketOrderPlaced.broker,
                marketOrderPlaced.quantity,
                marketOrderPlaced.quantity,
                0,
                marketOrderPlaced.side,
                0,
                0,
                marketOrderPlaced.symbol,
                SUBMITTING,
                MARKET
        );
        orders.put(orderUpdated.id, orderUpdated);
        orderListener.onOrderUpdated(orderUpdated);
    }

    @Override
    public void onLimitOrderPlaced(LimitOrderAccepted limitOrderAccepted) {
        OrderUpdated orderUpdated = new OrderUpdated(
                limitOrderAccepted.id,
                limitOrderAccepted.time,
                limitOrderAccepted.broker,
                limitOrderAccepted.quantity,
                limitOrderAccepted.quantity,
                0,
                limitOrderAccepted.side,
                limitOrderAccepted.price,
                0,
                limitOrderAccepted.symbol,
                SUBMITTING,
                LIMIT
        );
        orders.put(orderUpdated.id, orderUpdated);
        orderListener.onOrderUpdated(orderUpdated);
    }

    @Override
    public void onTradeExecuted(TradeExecuted tradeExecuted) {
        OrderUpdated buyOrderUpdated = orders.computeIfPresent(tradeExecuted.buyingId, (id, buyOrder) -> new OrderUpdated(
                id,
                buyOrder.time,
                tradeExecuted.buyingBroker,
                buyOrder.requestedAmount,
                buyOrder.leftAmount - tradeExecuted.quantity,
                buyOrder.amount + tradeExecuted.quantity,
                BUY,
                buyOrder.limit,
                computeAveragePrice(buyOrder.price, buyOrder.amount, tradeExecuted.price, tradeExecuted.quantity),
                tradeExecuted.symbol,
                updateStatus(buyOrder.leftAmount, tradeExecuted.quantity),
                buyOrder.type
        ));
        orderListener.onOrderUpdated(buyOrderUpdated);

        OrderUpdated sellOrderUpdated = orders.computeIfPresent(tradeExecuted.sellingId, (id, sellOrder) -> new OrderUpdated(
                id,
                sellOrder.time,
                tradeExecuted.sellingBroker,
                sellOrder.requestedAmount,
                sellOrder.leftAmount - tradeExecuted.quantity,
                sellOrder.amount + tradeExecuted.quantity,
                SELL,
                sellOrder.limit,
                computeAveragePrice(sellOrder.price, sellOrder.amount, tradeExecuted.price, tradeExecuted.quantity),
                tradeExecuted.symbol,
                updateStatus(sellOrder.leftAmount, tradeExecuted.quantity),
                sellOrder.type
        ));

        orderListener.onOrderUpdated(sellOrderUpdated);
    }

    @Override
    public void onMarketOrderRejected(MarketOrderRejected marketOrderRejected) {
        OrderUpdated order = orders.computeIfPresent(marketOrderRejected.id, (id, orderUpdated) -> new OrderUpdated(
                orderUpdated.id,
                orderUpdated.time,
                orderUpdated.broker,
                orderUpdated.requestedAmount,
                orderUpdated.leftAmount,
                orderUpdated.amount,
                orderUpdated.direction,
                orderUpdated.limit,
                orderUpdated.price,
                orderUpdated.symbol,
                CANCELLED,
                orderUpdated.type
        ));
        orderListener.onOrderUpdated(order);

    }

    public void onSubscribe() {
        orders.forEach((id, orderUpdated) -> orderListener.onOrderUpdated(orderUpdated));
    }

    private OrderUpdated.Status updateStatus(int leftAmount, int quantity) {
        return leftAmount - quantity > 0 ? WORKING : DONE;
    }

    private double computeAveragePrice(double price, int quantity, double executedPrice, int executedQuantity) {
        double avgPrice = (price * quantity + executedPrice * executedQuantity) / (quantity + executedQuantity);
        double pow = Math.pow(10, 5);
        return Math.round(avgPrice * pow) / pow;
    }

}
