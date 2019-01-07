package org.trading.messaging.serializer;

import com.google.protobuf.util.Timestamps;
import org.trading.api.event.TradeExecuted;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class TradeExecutedFromProtobuf {

    public Either<String, TradeExecuted> fromProtobuf(org.trading.TradeExecuted tradeExecuted) {

        return Either.right(new org.trading.api.event.TradeExecuted(
                UUID.fromString(tradeExecuted.getBuyingId()),
                tradeExecuted.getBuyingBroker(),
                UUID.fromString(tradeExecuted.getSellingId()),
                tradeExecuted.getSellingBroker(),
                tradeExecuted.getQuantity(),
                tradeExecuted.getPrice(),
                tradeExecuted.getBuyingLimit(),
                tradeExecuted.getSellingLimit(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(Timestamps.toMillis(tradeExecuted.getTime())), UTC),
                tradeExecuted.getSymbol()
        ));
    }
}
