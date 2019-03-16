package org.trading.matching.engine.view;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.trading.MessageProvider;
import org.trading.messaging.Message;

import static org.slf4j.LoggerFactory.getLogger;

public class ViewStore implements EventHandler<Message> {
    private static final Logger LOGGER = getLogger(ViewStore.class);
    private final ViewRepository viewRepository;

    public ViewStore(ViewRepository viewRepository) {
        this.viewRepository = viewRepository;
    }

    @Override
    public void onEvent(Message event, long sequence, boolean endOfBatch) {
        MessageProvider.Message.Builder messageBuilder = MessageProvider.Message.newBuilder();

        event.type.accept(new Message.EventType.EventTypeVisitor<Void>() {
            @Override
            public Void visitSubmitOrder() {
                messageBuilder.setEvenType(MessageProvider.EventType.SUBMIT_ORDER);
                messageBuilder.setSubmitOrder((MessageProvider.SubmitOrder) event.event);
                return null;
            }

            @Override
            public Void visitSubscribe() {
                return null;
            }

            @Override
            public Void visitMarketOrderAccepted() {
                messageBuilder.setEvenType(MessageProvider.EventType.MARKET_ORDER_ACCEPTED);
                messageBuilder.setMarketOrderAccepted((MessageProvider.MarketOrderAccepted) event.event);
                return null;
            }

            @Override
            public Void visitLimitOrderAccepted() {
                messageBuilder.setEvenType(MessageProvider.EventType.LIMIT_ORDER_ACCEPTED);
                messageBuilder.setLimitOrderAccepted((MessageProvider.LimitOrderAccepted) event.event);
                return null;
            }

            @Override
            public Void visitMarketOrderRejected() {
                messageBuilder.setEvenType(MessageProvider.EventType.MARKET_ORDER_REJECTED);
                messageBuilder.setMarketOrderRejected((MessageProvider.MarketOrderRejected) event.event);
                return null;
            }

            @Override
            public Void visitTradeExecuted() {
                messageBuilder.setEvenType(MessageProvider.EventType.TRADE_EXECUTED);
                messageBuilder.setTradeExecuted((MessageProvider.TradeExecuted) event.event);
                return null;
            }

            @Override
            public Void visitRequestExecution() {
                return null;
            }

            @Override
            public Void visitUpdateQuantities() {
                return null;
            }

            @Override
            public Void visitOrderBookCreated() {
                return null;
            }
        });
        viewRepository.store(messageBuilder.build());
    }
}
