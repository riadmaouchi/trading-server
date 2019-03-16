package org.trading.matching.engine.translate;

import com.google.protobuf.util.Timestamps;
import org.trading.MessageProvider;
import org.trading.MessageProvider.OrderType;
import org.trading.api.message.OrderType.OrderTypeVisitor;
import org.trading.matching.engine.domain.Trade;
import org.trading.messaging.Message;

import static java.time.ZoneOffset.UTC;
import static org.trading.MessageProvider.OrderType.*;

public class TradeExecutedTranslator {

    private static final OrderTypeVisitor<OrderType> orderTypeVisitor = new OrderTypeVisitor<>() {
        @Override
        public OrderType visitMarket() {
            return MARKET;
        }

        @Override
        public OrderType visitLimit() {
            return LIMIT;
        }
    };

    public static void translateTo(Message message, long sequence, Trade trade) {
        message.event = MessageProvider.TradeExecuted.newBuilder()
                .setSymbol(trade.symbol)
                .setBuyingId(trade.buyingId.toString())
                .setBuyingLimit(trade.buyingLimit)
                .setPrice(trade.price)
                .setQuantity(trade.quantity)
                .setSellingId(trade.sellingId.toString())
                .setBuyingBroker(trade.buyingBroker)
                .setSellingLimit(trade.sellingLimit)
                .setSellingBroker(trade.sellingBroker)
                .setTime(Timestamps.fromSeconds(trade.time.toEpochSecond(UTC)))
                .setSellingOrderType(trade.sellingOrderType.accept(orderTypeVisitor))
                .setBuyingOrderType(trade.buyingOrderType.accept(orderTypeVisitor))
                .build();
        message.type = Message.EventType.TRADE_EXECUTED;
    }
}
