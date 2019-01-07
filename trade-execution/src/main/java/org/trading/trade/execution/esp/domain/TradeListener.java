package org.trading.trade.execution.esp.domain;

public interface TradeListener {

    void onExecutionAccepted(ExecutionAccepted executionAccepted);

    void onExecutionRejected(ExecutionRejected executionRejected);
}
