package org.trading.messaging.serializer;

import org.trading.MessageProvider.OrderType;
import org.trading.MessageProvider.Side;
import org.trading.MessageProvider.SubmitOrder;
import org.trading.api.message.OrderType.OrderTypeVisitor;
import org.trading.api.message.Side.SideVisitor;

public class SubmitOrderToProtobuf {

    public SubmitOrder toProtobuf(org.trading.api.message.SubmitOrder submitOrder) {

        return SubmitOrder.newBuilder()
                .setSymbol(submitOrder.symbol)
                .setSide(submitOrder.side.accept(sideSideVisitor))
                .setBroker(submitOrder.broker)
                .setOrderType(submitOrder.orderType.accept(orderTypeVisitor))
                .setAmount(submitOrder.amount)
                .build();
    }

    private final SideVisitor<Side> sideSideVisitor = new SideVisitor<>() {
        @Override
        public Side visitBuy() {
            return Side.BUY;
        }

        @Override
        public Side visitSell() {
            return Side.SELL;
        }
    };

    private final OrderTypeVisitor<OrderType> orderTypeVisitor = new OrderTypeVisitor<>() {
        @Override
        public OrderType visitMarket() {
            return OrderType.MARKET;
        }

        @Override
        public OrderType visitLimit() {
            return OrderType.LIMIT;
        }
    };
}
