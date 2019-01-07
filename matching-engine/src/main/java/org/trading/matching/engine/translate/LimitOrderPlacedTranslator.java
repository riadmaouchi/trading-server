package org.trading.matching.engine.translate;

import com.google.protobuf.util.Timestamps;
import org.trading.Side;
import org.trading.api.command.Side.SideVisitor;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.messaging.Message;

import static java.time.ZoneOffset.UTC;

public class LimitOrderPlacedTranslator {

    public static void translateTo(Message message, long sequence, LimitOrderPlaced limitOrderPlaced) {
        message.event = org.trading.LimitOrderPlaced.newBuilder()
                .setId(limitOrderPlaced.id.toString())
                .setTime(Timestamps.fromSeconds(limitOrderPlaced.time.toEpochSecond(UTC)))
                .setSymbol(limitOrderPlaced.symbol)
                .setQuantity(limitOrderPlaced.quantity)
                .setBroker(limitOrderPlaced.broker)
                .setLimit(limitOrderPlaced.price)
                .setSide(limitOrderPlaced.side.accept(new SideVisitor<>() {
                    @Override
                    public Side visitBuy() {
                        return org.trading.Side.BUY;
                    }

                    @Override
                    public Side visitSell() {
                        return org.trading.Side.SELL;
                    }
                })).build();
        message.type = Message.EventType.LIMIT_ORDER_PLACED;
    }
}
