package org.trading.messaging.serializer;

import com.google.protobuf.util.Timestamps;
import org.trading.MessageProvider;
import org.trading.api.SideVisitor;
import org.trading.api.event.LimitOrderAccepted;
import org.trading.api.message.Side;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class LimitOrderAcceptedFromProtobuf {

    private final SideVisitor<MessageProvider.Side, Either<String, Side>> sideVisitor;

    public LimitOrderAcceptedFromProtobuf(SideVisitor<MessageProvider.Side, Either<String, Side>> sideVisitor) {
        this.sideVisitor = sideVisitor;
    }

    public Either<String, LimitOrderAccepted> fromProtobuf(MessageProvider.LimitOrderAccepted limitOrderAccepted) {
        Either<String, Side> sideEither = sideVisitor.visit(limitOrderAccepted.getSide(), limitOrderAccepted.getSide());

        if (sideEither.isLeft()) {
            return Either.left(sideEither.left());
        }

        return Either.right(new LimitOrderAccepted(
                UUID.fromString(limitOrderAccepted.getId()),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(Timestamps.toMillis(limitOrderAccepted.getTime())), UTC),
                limitOrderAccepted.getBroker(),
                limitOrderAccepted.getQuantity(),
                sideEither.right(),
                limitOrderAccepted.getLimit(),
                limitOrderAccepted.getSymbol()
        ));
    }
}
