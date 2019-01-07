package org.trading.messaging.serializer;

import com.google.protobuf.util.Timestamps;
import org.trading.api.SideVisitor;
import org.trading.api.command.Side;
import org.trading.api.event.LimitOrderPlaced;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class LimitOrderPlacedFromProtobuf {

    private final SideVisitor<org.trading.Side, Either<String, Side>> sideVisitor;

    public LimitOrderPlacedFromProtobuf(SideVisitor<org.trading.Side, Either<String, Side>> sideVisitor) {
        this.sideVisitor = sideVisitor;
    }

    public Either<String, LimitOrderPlaced> fromProtobuf(org.trading.LimitOrderPlaced limitOrderPlaced) {
        Either<String, Side> sideEither = sideVisitor.visit(limitOrderPlaced.getSide(), limitOrderPlaced.getSide());

        if (sideEither.isLeft()) {
            return Either.left(sideEither.left());
        }

        return Either.right(new LimitOrderPlaced(
                UUID.fromString(limitOrderPlaced.getId()),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(Timestamps.toMillis(limitOrderPlaced.getTime())), UTC),
                limitOrderPlaced.getBroker(),
                limitOrderPlaced.getQuantity(),
                sideEither.right(),
                limitOrderPlaced.getLimit(),
                limitOrderPlaced.getSymbol()
        ));
    }
}
