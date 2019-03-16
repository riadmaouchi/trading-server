package org.trading.matching.engine;

import com.lmax.disruptor.dsl.Disruptor;
import org.trading.matching.engine.domain.OrderBook;
import org.trading.matching.engine.api.ExchangeResponseListener;
import org.trading.matching.engine.translate.LimitOrderAcceptedTranslator;
import org.trading.matching.engine.translate.MarketOrderAcceptedTranslator;
import org.trading.matching.engine.translate.MarketOrderRejectedTranslator;
import org.trading.matching.engine.translate.TradeExecutedTranslator;
import org.trading.messaging.Message;

public class ExchangeResponsePublisher implements ExchangeResponseListener {

    private final Disruptor<Message> disruptor;

    ExchangeResponsePublisher(Disruptor<Message> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void onOrderBookCreated(OrderBook.OrderBookCreated orderBookCreated) {

    }

    @Override
    public void onMarketOrderAccepted(OrderBook.MarketOrderAccepted marketOrderAccepted) {
        disruptor.publishEvent(MarketOrderAcceptedTranslator::translateTo, marketOrderAccepted.marketOrder);
    }

    @Override
    public void onMarketOrderRejected(OrderBook.MarketOrderRejected marketOrderRejected) {
        disruptor.publishEvent(MarketOrderRejectedTranslator::translateTo, marketOrderRejected.marketOrder, marketOrderRejected.fillStatus);
    }

    @Override
    public void onLimitOrderAccepted(OrderBook.LimitOrderAccepted limitOrderAccepted) {
        disruptor.publishEvent(LimitOrderAcceptedTranslator::translateTo, limitOrderAccepted.limitOrder);
    }

    @Override
    public void onBuyLimitOrderPlaced(OrderBook.BuyLimitOrderPlaced buyLimitOrderPlaced) {
        // disruptor.publishEvent(LimitOrderAcceptedTranslator::translateTo, buyLimitOrderPlaced.limitOrder);
    }


    @Override
    public void onSellLimitOrderPlaced(OrderBook.SellLimitOrderPlaced sellLimitOrderPlaced) {
        // disruptor.publishEvent(LimitOrderAcceptedTranslator::translateTo, sellLimitOrderPlaced.limitOrder);
    }

    @Override
    public void onTradeExecuted(OrderBook.TradeExecuted tradeExecuted) {
        disruptor.publishEvent(TradeExecutedTranslator::translateTo, tradeExecuted.trade);
    }
}
