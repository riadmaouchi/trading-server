package org.trading.messaging.serializer;

import com.google.protobuf.util.Timestamps;
import org.trading.MessageProvider;
import org.trading.api.event.TradeExecuted;
import org.trading.api.message.OrderType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class TradeExecutedFromProtobuf {

    private final OrderTypeFromProtobuf orderTypeFromProtobuf = new OrderTypeFromProtobuf();

    public Either<String, TradeExecuted> fromProtobuf(MessageProvider.TradeExecuted tradeExecuted) {

        Either<String, OrderType> buyingOrderTypeEither = orderTypeFromProtobuf.visit(tradeExecuted.getBuyingOrderType(), tradeExecuted.getBuyingOrderType());

        if (buyingOrderTypeEither.isLeft()) {
            return Either.left(buyingOrderTypeEither.left());
        }

        Either<String, OrderType> sellingOrderTypeEither = orderTypeFromProtobuf.visit(tradeExecuted.getSellingOrderType(), tradeExecuted.getSellingOrderType());

        if (sellingOrderTypeEither.isLeft()) {
            return Either.left(sellingOrderTypeEither.left());
        }

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
                tradeExecuted.getSymbol(),
                buyingOrderTypeEither.right(),
                sellingOrderTypeEither.right()
        ));
    }
}
