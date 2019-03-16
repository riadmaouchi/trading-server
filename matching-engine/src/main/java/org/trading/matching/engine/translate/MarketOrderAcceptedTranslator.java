package org.trading.matching.engine.translate;

import org.trading.MessageProvider;
import org.trading.MessageProvider.Side;
import org.trading.api.message.Side.SideVisitor;
import org.trading.matching.engine.domain.MarketOrder;
import org.trading.messaging.Message;

import static com.google.protobuf.util.Timestamps.fromSeconds;
import static java.time.ZoneOffset.UTC;
import static org.trading.MessageProvider.Side.BUY;
import static org.trading.MessageProvider.Side.SELL;
import static org.trading.messaging.Message.EventType.MARKET_ORDER_ACCEPTED;

public class MarketOrderAcceptedTranslator {

    public static void translateTo(Message message, long sequence, MarketOrder marketOrder) {
        message.type = MARKET_ORDER_ACCEPTED;
        message.event = MessageProvider.MarketOrderAccepted.newBuilder()
                .setId(marketOrder.id.toString())
                .setTime(fromSeconds(marketOrder.time.toEpochSecond(UTC)))
                .setSymbol(marketOrder.symbol)
                .setQuantity(marketOrder.quantity)
                .setBroker(marketOrder.broker)
                .setSide(marketOrder.side.accept(new SideVisitor<>() {
                    @Override
                    public Side visitBuy() {
                        return BUY;
                    }

                    @Override
                    public Side visitSell() {
                        return SELL;
                    }
                }))
                .build();
    }
}
