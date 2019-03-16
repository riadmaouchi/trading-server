package org.trading.messaging.serializer;

import org.trading.MessageProvider;
import org.trading.api.OrderTypeVisitor;
import org.trading.api.SideVisitor;
import org.trading.api.message.Side;
import org.trading.api.message.SubmitOrder;

import static org.trading.api.message.SubmitOrder.aSubmitLimitOrder;

public class SubmitOrderFromProtobuf {

    private final SideVisitor<MessageProvider.Side, Either<String, Side>> sideVisitor;

    public SubmitOrderFromProtobuf(SideVisitor<MessageProvider.Side, Either<String, Side>> sideVisitor) {
        this.sideVisitor = sideVisitor;
    }

    public Either<String, SubmitOrder> fromProtobuf(MessageProvider.SubmitOrder submitOrder) {
        OrderTypeVisitor<MessageProvider.SubmitOrder, Either<String, SubmitOrder>> orderTypeVisitor = new OrderTypeVisitor<>() {

            @Override
            public Either<String, SubmitOrder> visitLimitOrder(MessageProvider.SubmitOrder submitOrder) {
                Either<String, Side> sideEither = sideVisitor.visit(submitOrder.getSide(), submitOrder.getSide());

                if (sideEither.isLeft()) {
                    return Either.left(sideEither.left());
                }

                return Either.right(aSubmitLimitOrder(
                        submitOrder.getSymbol(),
                        submitOrder.getBroker(),
                        submitOrder.getAmount(),
                        sideEither.right(),
                        submitOrder.getPrice()
                ));
            }

            @Override
            public Either<String, SubmitOrder> visitMarketOrder(MessageProvider.SubmitOrder submitOrder) {
                Either<String, Side> sideEither = sideVisitor.visit(submitOrder.getSide(), submitOrder.getSide());

                if (sideEither.isLeft()) {
                    return Either.left(sideEither.left());
                }

                return Either.right(SubmitOrder.aSubmitMarketOrder(
                        submitOrder.getSymbol(),
                        submitOrder.getBroker(),
                        submitOrder.getAmount(),
                        sideEither.right()
                ));
            }

            @Override
            public Either<String, SubmitOrder> visitUnknownValue(MessageProvider.SubmitOrder submitOrder) {
                return Either.left("Unknown order type : " + submitOrder.getOrderType());
            }
        };
        return orderTypeVisitor.visit(submitOrder.getOrderType(), submitOrder);
    }
}
