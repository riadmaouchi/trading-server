package org.trading.trade.execution.esp.translate;

import org.trading.messaging.Message;
import org.trading.trade.execution.esp.domain.ExecutionRequest;

import static org.trading.messaging.Message.EventType.REQUEST_EXECUTION;

public final class ExecutionRequestTranslator {

    private ExecutionRequestTranslator() {
    }

    public static void translateTo(Message message, long sequence, ExecutionRequest executionRequest) {
        message.event = executionRequest;
        message.type = REQUEST_EXECUTION;
    }
}
