package org.trading.messaging.serializer;

import org.trading.SubmitOrder;
import org.trading.api.command.OrderType;
import org.trading.api.command.Side;

public class SubmitOrderToProtobuf {

    public SubmitOrder toProtobuf(org.trading.api.command.SubmitOrder submitOrder) {
        return SubmitOrder.newBuilder()
                .setSymbol(submitOrder.symbol)
                .setSide(submitOrder.side.accept(new Side.SideVisitor<>() {
                    @Override
                    public org.trading.Side visitBuy() {
                        return org.trading.Side.BUY;
                    }

                    @Override
                    public org.trading.Side visitSell() {
                        return org.trading.Side.SELL;
                    }
                }))
                .setBroker(submitOrder.broker)
                .setOrderType(submitOrder.orderType.accept(new OrderType.OrderTypeVisitor<>() {
                    @Override
                    public org.trading.OrderType visitMarket() {
                        return org.trading.OrderType.MARKET;
                    }

                    @Override
                    public org.trading.OrderType visitLimit() {
                        return org.trading.OrderType.LIMIT;
                    }
                }))
                .setAmount(submitOrder.amount)
                .build();

    }
}
