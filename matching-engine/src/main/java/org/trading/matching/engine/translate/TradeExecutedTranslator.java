package org.trading.matching.engine.translate;

import com.google.protobuf.util.Timestamps;
import org.trading.api.event.TradeExecuted;
import org.trading.messaging.Message;

import static java.time.ZoneOffset.UTC;

public class TradeExecutedTranslator {

    public static void translateTo(Message message, long sequence, TradeExecuted tradeExecuted) {
        message.event = org.trading.TradeExecuted.newBuilder()
                .setSymbol(tradeExecuted.symbol)
                .setBuyingId(tradeExecuted.buyingId.toString())
                .setBuyingLimit(tradeExecuted.buyingLimit)
                .setPrice(tradeExecuted.price)
                .setQuantity(tradeExecuted.quantity)
                .setSellingId(tradeExecuted.sellingId.toString())
                .setBuyingBroker(tradeExecuted.buyingBroker)
                .setSellingLimit(tradeExecuted.sellingLimit)
                .setSellingBroker(tradeExecuted.sellingBroker)
                .setTime(Timestamps.fromSeconds(tradeExecuted.time.toEpochSecond(UTC)))
                .build();
        message.type = Message.EventType.TRADE_EXECUTED;
    }
}
