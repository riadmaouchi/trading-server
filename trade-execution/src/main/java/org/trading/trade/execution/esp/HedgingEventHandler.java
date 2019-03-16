package org.trading.trade.execution.esp;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.trading.MessageProvider.SubmitOrder;
import org.trading.messaging.Message;
import org.trading.messaging.serializer.SubmitOrderToProtobuf;
import org.trading.trade.execution.esp.TradeMessage.EventType.EventTypeVisitor;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;
import org.trading.trade.execution.esp.translate.SubmitOrderTranslator;

import static org.trading.api.message.SubmitOrder.aSubmitMarketOrder;

public class HedgingEventHandler implements EventHandler<TradeMessage> {

    private final Disruptor<Message> disruptor;
    private final SubmitOrderToProtobuf submitOrderToProtobuf = new SubmitOrderToProtobuf();

    public HedgingEventHandler(Disruptor<Message> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void onEvent(TradeMessage tradeMessage, long sequence, boolean endOfBatch) {
        tradeMessage.type.accept(new EventTypeVisitor<Void>() {
            @Override
            public Void visitExecutionAccepted() {
                ExecutionAccepted executionAccepted = (ExecutionAccepted) tradeMessage.event;
                org.trading.api.message.SubmitOrder order = aSubmitMarketOrder(
                        executionAccepted.symbol,
                        "Hedging Service",
                        executionAccepted.quantity,
                        executionAccepted.side
                );
                SubmitOrder submitOrder = submitOrderToProtobuf.toProtobuf(order);
                disruptor.publishEvent(SubmitOrderTranslator::translateTo, submitOrder);
                return null;
            }

            @Override
            public Void visitExecutionRejected() {
                // nop
                return null;
            }
        });
    }
}
