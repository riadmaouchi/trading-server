package org.trading.matching.engine.translate;

import org.trading.Side;
import org.trading.api.command.Side.SideVisitor;
import org.trading.api.event.MarketOrderPlaced;
import org.trading.messaging.Message;

import static com.google.protobuf.util.Timestamps.fromSeconds;
import static java.time.ZoneOffset.UTC;
import static org.trading.Side.BUY;
import static org.trading.Side.SELL;
import static org.trading.messaging.Message.EventType.MARKET_ORDER_PLACED;

public class MarketOrderPlacedTranslator {

    public static void translateTo(Message message, long sequence, MarketOrderPlaced marketOrderPlaced) {
        message.type = MARKET_ORDER_PLACED;
        message.event = org.trading.MarketOrderPlaced.newBuilder()
                .setId(marketOrderPlaced.id.toString())
                .setTime(fromSeconds(marketOrderPlaced.time.toEpochSecond(UTC)))
                .setSymbol(marketOrderPlaced.symbol)
                .setQuantity(marketOrderPlaced.quantity)
                .setBroker(marketOrderPlaced.broker)
                .setSide(marketOrderPlaced.side.accept(new SideVisitor<>() {
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
