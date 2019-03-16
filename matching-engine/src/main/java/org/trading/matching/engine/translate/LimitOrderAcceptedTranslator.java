package org.trading.matching.engine.translate;

import com.google.protobuf.util.Timestamps;
import org.trading.MessageProvider;
import org.trading.MessageProvider.Side;
import org.trading.api.message.Side.SideVisitor;
import org.trading.matching.engine.domain.LimitOrder;
import org.trading.messaging.Message;

import static java.time.ZoneOffset.UTC;
import static org.trading.messaging.Message.EventType.LIMIT_ORDER_ACCEPTED;

public class LimitOrderAcceptedTranslator {

    public static void translateTo(Message message, long sequence, LimitOrder limitOrder) {
        message.event = MessageProvider.LimitOrderAccepted.newBuilder()
                .setId(limitOrder.id.toString())
                .setTime(Timestamps.fromSeconds(limitOrder.time.toEpochSecond(UTC)))
                .setSymbol(limitOrder.symbol)
                .setQuantity(limitOrder.quantity)
                .setBroker(limitOrder.broker)
                .setLimit(limitOrder.limit)
                .setSide(limitOrder.side.accept(new SideVisitor<>() {
                    @Override
                    public Side visitBuy() {
                        return MessageProvider.Side.BUY;
                    }

                    @Override
                    public Side visitSell() {
                        return MessageProvider.Side.SELL;
                    }
                })).build();
        message.type = LIMIT_ORDER_ACCEPTED;
    }
}
