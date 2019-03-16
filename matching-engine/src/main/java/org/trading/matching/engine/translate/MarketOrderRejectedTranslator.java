package org.trading.matching.engine.translate;

import org.trading.MessageProvider;
import org.trading.api.message.FillStatus;
import org.trading.api.message.FillStatus.FillStatusVisitor;
import org.trading.matching.engine.domain.MarketOrder;
import org.trading.messaging.Message;

import static com.google.protobuf.util.Timestamps.fromSeconds;
import static java.time.ZoneOffset.UTC;
import static org.trading.messaging.Message.EventType.MARKET_ORDER_REJECTED;

public class MarketOrderRejectedTranslator {

    public static void translateTo(Message message, long sequence, MarketOrder marketOrder, FillStatus fillStatus) {
        message.type = MARKET_ORDER_REJECTED;
        message.event = MessageProvider.MarketOrderRejected.newBuilder()
                .setId(marketOrder.id.toString())
                .setTime(fromSeconds(marketOrder.time.toEpochSecond(UTC)))
                .setFillStatus(fillStatus.accept(new FillStatusVisitor<>() {

                    @Override
                    public MessageProvider.FillStatus visitFullyFilled() {
                        return MessageProvider.FillStatus.FULLY_FILLED;
                    }

                    @Override
                    public MessageProvider.FillStatus visitPartiallyFilled() {
                        return MessageProvider.FillStatus.PARTIALLY_FILLED;
                    }
                }))
                .build();
    }
}
