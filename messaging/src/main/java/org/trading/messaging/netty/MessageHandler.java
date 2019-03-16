package org.trading.messaging.netty;

import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.trading.MessageProvider.EventType;
import org.trading.MessageProvider.LimitOrderAccepted;
import org.trading.MessageProvider.MarketOrderAccepted;
import org.trading.MessageProvider.MarketOrderRejected;
import org.trading.MessageProvider.Message;
import org.trading.MessageProvider.SubmitOrder;
import org.trading.MessageProvider.TradeExecuted;
import org.trading.api.MessageTypeVisitor;
import org.trading.messaging.serializer.Either;
import org.trading.messaging.serializer.LimitOrderAcceptedFromProtobuf;
import org.trading.messaging.serializer.MarketOrderAcceptedFromProtobuf;
import org.trading.messaging.serializer.MarketOrderRejectedFromProtobuf;
import org.trading.messaging.serializer.SideFromProtobuf;
import org.trading.messaging.serializer.SubmitOrderFromProtobuf;
import org.trading.messaging.serializer.TradeExecutedFromProtobuf;
import org.trading.messaging.translate.LimitOrderPlacedTranslator;
import org.trading.messaging.translate.MarketOrderPlacedTranslator;
import org.trading.messaging.translate.MarketOrderRejectedTranslator;
import org.trading.messaging.translate.SubmitOrderTranslator;
import org.trading.messaging.translate.TradeExecutedTranslator;

import static com.google.protobuf.TextFormat.shortDebugString;
import static org.slf4j.LoggerFactory.getLogger;

public class MessageHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger LOGGER = getLogger(MessageHandler.class);
    private final Disruptor<org.trading.messaging.Message> disruptor;
    private final SideFromProtobuf sideVisitor = new SideFromProtobuf();
    private final SubmitOrderFromProtobuf submitOrderFromProtobuf = new SubmitOrderFromProtobuf(sideVisitor);
    private final LimitOrderAcceptedFromProtobuf limitOrderAcceptedFromProtobuf = new LimitOrderAcceptedFromProtobuf(sideVisitor);
    private final MarketOrderAcceptedFromProtobuf marketOrderAcceptedFromProtobuf = new MarketOrderAcceptedFromProtobuf(sideVisitor);
    private final TradeExecutedFromProtobuf tradeExecutedFromProtobuf = new TradeExecutedFromProtobuf();
    private final MarketOrderRejectedFromProtobuf marketOrderRejectedFromProtobuf = new MarketOrderRejectedFromProtobuf();

    private final MessageTypeVisitor<Message, EventType> messageTypeVisitor = new MessageTypeVisitor<>() {

        @Override
        public EventType visitSubmitOrder(Message message) {
            SubmitOrder submitOrder = message.getSubmitOrder();
            Either<String, org.trading.api.message.SubmitOrder> submitOrderEither = submitOrderFromProtobuf.fromProtobuf(submitOrder);
            submitOrderEither.fold(
                    reason -> LOGGER.warn("Submit order ignored : {}", reason),
                    order -> disruptor.publishEvent(SubmitOrderTranslator::translateTo, order)
            );
            return null;
        }

        @Override
        public EventType visitLimitOrderAccepted(Message message) {
            LimitOrderAccepted limitOrderAccepted = message.getLimitOrderAccepted();
            Either<String, org.trading.api.event.LimitOrderAccepted> limitOrderAcceptedEither = limitOrderAcceptedFromProtobuf.fromProtobuf(limitOrderAccepted);
            limitOrderAcceptedEither.fold(
                    reason -> LOGGER.warn("Limit order accepted ignored : {}", reason),
                    order -> disruptor.publishEvent(LimitOrderPlacedTranslator::translateTo, order)
            );
            return null;
        }

        @Override
        public EventType visitMarketOrderAccepted(Message message) {
            MarketOrderAccepted marketOrderAccepted = message.getMarketOrderAccepted();
            Either<String, org.trading.api.event.MarketOrderAccepted> marketOrderPlacedEither = marketOrderAcceptedFromProtobuf.fromProtobuf(marketOrderAccepted);
            marketOrderPlacedEither.fold(
                    reason -> LOGGER.warn("Market order placed ignored : {}", reason),
                    order -> disruptor.publishEvent(MarketOrderPlacedTranslator::translateTo, order)
            );
            return null;
        }

        @Override
        public EventType visitTradeExecuted(Message message) {
            TradeExecuted tradeExecuted = message.getTradeExecuted();
            Either<String, org.trading.api.event.TradeExecuted> tradeExecutedEither = tradeExecutedFromProtobuf.fromProtobuf(tradeExecuted);
            tradeExecutedEither.fold(
                    reason -> LOGGER.warn("Trade executed ignored : {}", reason),
                    order -> disruptor.publishEvent(TradeExecutedTranslator::translateTo, order)
            );
            return null;
        }

        @Override
        public EventType visitUnknownValue(Message message) {
            LOGGER.warn("Unknown message ignored : {}", shortDebugString(message));
            return null;
        }

        @Override
        public EventType visitMarketOrderRejected(Message message) {
            MarketOrderRejected marketOrderRejected = message.getMarketOrderRejected();
            Either<String, org.trading.api.event.MarketOrderRejected> orderRejectedEither = marketOrderRejectedFromProtobuf.fromProtobuf(marketOrderRejected);
            orderRejectedEither.fold(
                    reason -> LOGGER.warn("Market order rejected ignored : {}", reason),
                    order -> disruptor.publishEvent(MarketOrderRejectedTranslator::translateTo, order)
            );
            return null;
        }
    };

    MessageHandler(Disruptor<org.trading.messaging.Message> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) {
        messageTypeVisitor.visit(message.getEvenType(), message);
        LOGGER.debug(shortDebugString(message));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Error", cause);
        ctx.close();
    }

}
