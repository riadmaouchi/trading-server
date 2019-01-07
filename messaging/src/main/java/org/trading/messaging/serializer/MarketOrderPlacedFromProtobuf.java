package org.trading.messaging.serializer;

import com.google.protobuf.util.Timestamps;
import org.trading.api.SideVisitor;
import org.trading.api.command.Side;
import org.trading.api.event.MarketOrderPlaced;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class MarketOrderPlacedFromProtobuf {

    private final SideVisitor<org.trading.Side, Either<String, Side>> sideVisitor;

    public MarketOrderPlacedFromProtobuf(SideVisitor<org.trading.Side, Either<String, Side>> sideVisitor) {
        this.sideVisitor = sideVisitor;
    }

    public Either<String, MarketOrderPlaced> fromProtobuf(org.trading.MarketOrderPlaced marketOrderPlaced) {
        Either<String, Side> sideEither = sideVisitor.visit(marketOrderPlaced.getSide(), marketOrderPlaced.getSide());

        if (sideEither.isLeft()) {
            return Either.left(sideEither.left());
        }

        return Either.right(new MarketOrderPlaced(
                UUID.fromString(marketOrderPlaced.getId()),
                marketOrderPlaced.getBroker(),
                marketOrderPlaced.getQuantity(),
                sideEither.right(),
                marketOrderPlaced.getSymbol(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(Timestamps.toMillis(marketOrderPlaced.getTime())), UTC)
        ));
    }
}
