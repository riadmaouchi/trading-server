package org.trading.trade.execution.esp;

import com.lmax.disruptor.dsl.Disruptor;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;
import org.trading.trade.execution.esp.domain.ExecutionRejected;
import org.trading.trade.execution.esp.domain.TradeListener;
import org.trading.trade.execution.esp.translate.ExecutionAcceptedTranslator;
import org.trading.trade.execution.esp.translate.ExecutionRejectedTranslator;

public class TradePublisher implements TradeListener {

    private final Disruptor<TradeMessage> disruptor;

    public TradePublisher(Disruptor<TradeMessage> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void onExecutionAccepted(ExecutionAccepted executionAccepted) {
        disruptor.publishEvent(ExecutionAcceptedTranslator::translateTo, executionAccepted);
    }

    @Override
    public void onExecutionRejected(ExecutionRejected executionRejected) {
        disruptor.publishEvent(ExecutionRejectedTranslator::translateTo, executionRejected);
    }
}
