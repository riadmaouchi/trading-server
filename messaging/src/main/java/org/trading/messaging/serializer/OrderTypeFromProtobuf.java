package org.trading.messaging.serializer;

import org.trading.MessageProvider;
import org.trading.api.OrderTypeVisitor;
import org.trading.api.message.OrderType;

public class OrderTypeFromProtobuf implements OrderTypeVisitor<MessageProvider.OrderType, Either<String, OrderType>> {

    @Override
    public Either<String, OrderType> visitLimitOrder(MessageProvider.OrderType orderType) {
        return Either.right(OrderType.LIMIT);
    }

    @Override
    public Either<String, OrderType> visitMarketOrder(MessageProvider.OrderType orderType) {
        return Either.right(OrderType.MARKET);
    }

    @Override
    public Either<String, OrderType> visitUnknownValue(MessageProvider.OrderType orderType) {
        return Either.left("Unknown orderType : " + orderType);
    }
}
