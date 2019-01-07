package org.trading.trade.execution.esp.translate;

import org.trading.trade.execution.esp.TradeMessage;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;
import org.trading.trade.execution.esp.domain.ExecutionRejected;

import static org.trading.trade.execution.esp.TradeMessage.EventType.EXECUTION_ACCEPTED;
import static org.trading.trade.execution.esp.TradeMessage.EventType.EXECUTION_REJECTED;

public class ExecutionRejectedTranslator {

    private ExecutionRejectedTranslator() {
    }

    public static void translateTo(TradeMessage message, long sequence, ExecutionRejected executionRejected) {
        message.event = executionRejected;
        message.type = EXECUTION_REJECTED;
    }
}
