package org.trading.trade.execution.esp;

import com.lmax.disruptor.EventHandler;
import org.trading.trade.execution.esp.TradeMessage.EventType.EventTypeVisitor;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;
import org.trading.trade.execution.esp.domain.ExecutionRejected;
import org.trading.trade.execution.esp.web.json.ExecutionAcceptedToJson;
import org.trading.trade.execution.esp.web.json.ExecutionRejectedToJson;
import org.trading.web.SseEventDispatcher;

public class ReportExecutionEventHandler implements EventHandler<TradeMessage> {

    private final SseEventDispatcher eventDispatcher;
    private final ExecutionAcceptedToJson executionAcceptedToJson = new ExecutionAcceptedToJson();
    private final ExecutionRejectedToJson executionRejectedToJson = new ExecutionRejectedToJson();

    public ReportExecutionEventHandler(SseEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void onEvent(TradeMessage tradeMessage, long sequence, boolean endOfBatch) {
        String json = tradeMessage.type.accept(new EventTypeVisitor<>() {
            @Override
            public String visitExecutionAccepted() {
                ExecutionAccepted executionAccepted = (ExecutionAccepted) tradeMessage.event;
                return executionAcceptedToJson.toJson(executionAccepted).toJSONString();
            }

            @Override
            public String visitExecutionRejected() {
                ExecutionRejected executionRejected = (ExecutionRejected) tradeMessage.event;
                return executionRejectedToJson.toJson(executionRejected).toJSONString();
            }
        });
        eventDispatcher.dispatchEvent("tradeReport", json);
    }
}
