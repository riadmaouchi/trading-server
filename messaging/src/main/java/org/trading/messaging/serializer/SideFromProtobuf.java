package org.trading.messaging.serializer;

import org.trading.MessageProvider;
import org.trading.api.SideVisitor;
import org.trading.api.message.Side;

public class SideFromProtobuf implements SideVisitor<MessageProvider.Side, Either<String, Side>> {

    @Override
    public Either<String, Side> visitBuy(MessageProvider.Side side) {
        return Either.right(Side.BUY);
    }

    @Override
    public Either<String, Side> visitSell(MessageProvider.Side side) {
        return Either.right(Side.SELL);
    }

    @Override
    public Either<String, Side> visitUnknownValue(MessageProvider.Side side) {
        return Either.left("Unknown side : " + side);
    }
}
