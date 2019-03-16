package org.trading.messaging.serializer;

import com.google.protobuf.util.Timestamps;
import org.trading.MessageProvider;
import org.trading.api.FillStatusVisitor;
import org.trading.api.event.MarketOrderRejected;
import org.trading.api.message.FillStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class MarketOrderRejectedFromProtobuf {


    public Either<String, MarketOrderRejected> fromProtobuf(MessageProvider.MarketOrderRejected marketOrderRejected) {
        Either<String, FillStatus> sideEither = new FillStatusVisitor<Either<String, FillStatus>>() {
            @Override
            public Either<String, FillStatus> visitFullyFilled() {
                return Either.right(FillStatus.FULLY_FILLED);
            }

            @Override
            public Either<String, FillStatus> visitPartiallyFilled() {
                return Either.right(FillStatus.PARTIALLY_FILLED);
            }

            @Override
            public Either<String, FillStatus> visitUnknownValue() {
                return Either.left("Unknown fill status : " + marketOrderRejected.getFillStatus());
            }
        }.visit(marketOrderRejected.getFillStatus());

        if (sideEither.isLeft()) {
            return Either.left(sideEither.left());
        }

        return Either.right(new MarketOrderRejected(
                UUID.fromString(marketOrderRejected.getId()),
                sideEither.right(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(Timestamps.toMillis(marketOrderRejected.getTime())), UTC)
        ));
    }
}
