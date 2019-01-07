package org.trading.messaging.serializer;

import org.trading.api.SideVisitor;
import org.trading.api.command.Side;

public class SideFromProtobuf implements SideVisitor<org.trading.Side, Either<String, Side>> {

    @Override
    public Either<String, Side> visitBuy(org.trading.Side side) {
        return Either.right(Side.BUY);
    }

    @Override
    public Either<String, Side> visitSell(org.trading.Side side) {
        return Either.right(Side.SELL);
    }

    @Override
    public Either<String, Side> visitUnknownValue(org.trading.Side side) {
        return Either.left("Unknown side : " + side);
    }
}
