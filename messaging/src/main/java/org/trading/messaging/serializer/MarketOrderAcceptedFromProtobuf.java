package org.trading.messaging.serializer;

import com.google.protobuf.util.Timestamps;
import org.trading.MessageProvider;
import org.trading.api.SideVisitor;
import org.trading.api.event.MarketOrderAccepted;
import org.trading.api.message.Side;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class MarketOrderAcceptedFromProtobuf {

    private final SideVisitor<MessageProvider.Side, Either<String, Side>> sideVisitor;

    public MarketOrderAcceptedFromProtobuf(SideVisitor<MessageProvider.Side, Either<String, Side>> sideVisitor) {
        this.sideVisitor = sideVisitor;
    }

    public Either<String, MarketOrderAccepted> fromProtobuf(MessageProvider.MarketOrderAccepted marketOrderPlaced) {
        Either<String, Side> sideEither = sideVisitor.visit(marketOrderPlaced.getSide(), marketOrderPlaced.getSide());

        if (sideEither.isLeft()) {
            return Either.left(sideEither.left());
        }

        return Either.right(new MarketOrderAccepted(
                UUID.fromString(marketOrderPlaced.getId()),
                marketOrderPlaced.getBroker(),
                marketOrderPlaced.getQuantity(),
                sideEither.right(),
                marketOrderPlaced.getSymbol(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(Timestamps.toMillis(marketOrderPlaced.getTime())), UTC)
        ));
    }
}
