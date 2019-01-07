package org.trading.messaging.netty;

import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.trading.*;
import org.trading.api.MessageTypeVisitor;
import org.trading.messaging.serializer.*;
import org.trading.messaging.translate.LimitOrderPlacedTranslator;
import org.trading.messaging.translate.MarketOrderPlacedTranslator;
import org.trading.messaging.translate.SubmitOrderTranslator;
import org.trading.messaging.translate.TradeExecutedTranslator;

import static com.google.protobuf.TextFormat.shortDebugString;
import static org.slf4j.LoggerFactory.getLogger;

public class MessageHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger LOGGER = getLogger(MessageHandler.class);
    private final Disruptor<org.trading.messaging.Message> disruptor;
    private final SideFromProtobuf sideVisitor = new SideFromProtobuf();
    private final SubmitOrderFromProtobuf submitOrderFromProtobuf = new SubmitOrderFromProtobuf(sideVisitor);
    private final LimitOrderPlacedFromProtobuf limitOrderPlacedFromProtobuf = new LimitOrderPlacedFromProtobuf(sideVisitor);
    private final MarketOrderPlacedFromProtobuf marketOrderPlacedFromProtobuf = new MarketOrderPlacedFromProtobuf(sideVisitor);
    private final TradeExecutedFromProtobuf tradeExecutedFromProtobuf = new TradeExecutedFromProtobuf();

    private final MessageTypeVisitor<Message, EventType> messageTypeVisitor = new MessageTypeVisitor<>() {

        @Override
        public EventType visitSubmitOrder(Message message) {
            SubmitOrder submitOrder = message.getSubmitOrder();
            Either<String, org.trading.api.command.SubmitOrder> submitOrderEither = submitOrderFromProtobuf.fromProtobuf(submitOrder);
            submitOrderEither.fold(
                    reason -> LOGGER.info("Submit order ignored : {}", reason),
                    order -> disruptor.publishEvent(SubmitOrderTranslator::translateTo, order)
            );
            return null;
        }

        @Override
        public EventType visitLimitOrderPlaced(Message message) {
            LimitOrderPlaced limitOrderPlaced = message.getLimitOrderPlaced();
            Either<String, org.trading.api.event.LimitOrderPlaced> limitOrderPlacedEither = limitOrderPlacedFromProtobuf.fromProtobuf(limitOrderPlaced);
            limitOrderPlacedEither.fold(
                    reason -> LOGGER.info("Limit order placed ignored : {}", reason),
                    order -> disruptor.publishEvent(LimitOrderPlacedTranslator::translateTo, order)
            );
            return null;
        }

        @Override
        public EventType visitMarketOrderPlaced(Message message) {
            MarketOrderPlaced marketOrderPlaced = message.getMarketOrderPlaced();
            Either<String, org.trading.api.event.MarketOrderPlaced> marketOrderPlacedEither = marketOrderPlacedFromProtobuf.fromProtobuf(marketOrderPlaced);
            marketOrderPlacedEither.fold(
                    reason -> LOGGER.info("Market order placed ignored : {}", reason),
                    order -> disruptor.publishEvent(MarketOrderPlacedTranslator::translateTo, order)
            );
            return null;
        }

        @Override
        public EventType visitTradeExecuted(Message message) {
            TradeExecuted tradeExecuted = message.getTradeExecuted();
            Either<String, org.trading.api.event.TradeExecuted> tradeExecutedEither = tradeExecutedFromProtobuf.fromProtobuf(tradeExecuted);
            tradeExecutedEither.fold(
                    reason -> LOGGER.info("Trade executed ignored : {}", reason),
                    order -> disruptor.publishEvent(TradeExecutedTranslator::translateTo, order)
            );
            return null;
        }

        @Override
        public EventType visitUnknownValue(Message message) {
            LOGGER.info("Unknown message ignored : {}", shortDebugString(message));
            return null;
        }
    };

    MessageHandler(Disruptor<org.trading.messaging.Message> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) {
        messageTypeVisitor.visit(message.getEvenType(), message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
