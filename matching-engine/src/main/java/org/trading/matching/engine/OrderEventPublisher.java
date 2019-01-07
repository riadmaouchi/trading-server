package org.trading.matching.engine;

import com.lmax.disruptor.dsl.Disruptor;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.MarketOrderPlaced;
import org.trading.api.event.TradeExecuted;
import org.trading.api.service.OrderEventListener;
import org.trading.messaging.Message;
import org.trading.matching.engine.translate.LimitOrderPlacedTranslator;
import org.trading.matching.engine.translate.MarketOrderPlacedTranslator;
import org.trading.matching.engine.translate.TradeExecutedTranslator;

public class OrderEventPublisher implements OrderEventListener {

    private final Disruptor<Message> disruptor;

    OrderEventPublisher(Disruptor<Message> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void onMarketOrderPlaced(MarketOrderPlaced marketOrderPlaced) {
        disruptor.publishEvent(MarketOrderPlacedTranslator::translateTo, marketOrderPlaced);
    }

    @Override
    public void onLimitOrderPlaced(LimitOrderPlaced limitOrderPlaced) {
        disruptor.publishEvent(LimitOrderPlacedTranslator::translateTo, limitOrderPlaced);
    }

    @Override
    public void onTradeExecuted(TradeExecuted tradeExecuted) {
        disruptor.publishEvent(TradeExecutedTranslator::translateTo, tradeExecuted);
    }
}
