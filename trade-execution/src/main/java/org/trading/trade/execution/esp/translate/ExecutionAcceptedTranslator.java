package org.trading.trade.execution.esp.translate;

import org.trading.trade.execution.esp.TradeMessage;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;

import static org.trading.trade.execution.esp.TradeMessage.EventType.EXECUTION_ACCEPTED;

public final class ExecutionAcceptedTranslator {

    private ExecutionAcceptedTranslator() {
    }

    public static void translateTo(TradeMessage message, long sequence, ExecutionAccepted executionAccepted) {
        message.event = executionAccepted;
        message.type = EXECUTION_ACCEPTED;
    }
}
