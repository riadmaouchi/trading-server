package org.trading.matching.engine.view;

import com.google.protobuf.Timestamp;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.protobuf.ProtobufDatumReader;
import org.apache.avro.protobuf.ProtobufDatumWriter;
import org.trading.MessageProvider;
import org.trading.MessageProvider.OrderType;
import org.trading.api.MessageTypeVisitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MessageSerializer {

    public byte[] serialize(MessageProvider.Message message) {
        ProtobufDatumWriter<MessageProvider.Message> datumWriter = new ProtobufDatumWriter<>(MessageProvider.Message.class);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Encoder e = EncoderFactory.get().binaryEncoder(os, null);
        try {
            datumWriter.write(message, e);
            e.flush();
            return os.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public MessageProvider.Message deserialize(byte[] bytes) {
        ProtobufDatumReader<MessageProvider.Message> datumReader = new ProtobufDatumReader<>(MessageProvider.Message.class);
        GenericDatumReader<GenericRecord> genericDatumReader = new GenericDatumReader<>(datumReader.getSchema());
        GenericRecord record;
        try {
            record = genericDatumReader.read(null, DecoderFactory.get().binaryDecoder(bytes, null));
            MessageProvider.Message.Builder message = MessageProvider.Message.newBuilder();
            MessageProvider.EventType evenType = MessageProvider.EventType.valueOf(record.get("evenType").toString());
            message.setEvenType(evenType);

            MessageProvider.Message.Builder builder = new MessageTypeVisitor<MessageProvider.EventType, MessageProvider.Message.Builder>() {
                @Override
                public MessageProvider.Message.Builder visitSubmitOrder(MessageProvider.EventType eventType) {
                    return null;
                }

                @Override
                public MessageProvider.Message.Builder visitLimitOrderAccepted(MessageProvider.EventType eventType) {
                    return createLimitOrderAccepted(record, message);
                }

                @Override
                public MessageProvider.Message.Builder visitMarketOrderAccepted(MessageProvider.EventType eventType) {
                    return createMarketOrderAccepted(record, message);
                }

                @Override
                public MessageProvider.Message.Builder visitTradeExecuted(MessageProvider.EventType eventType) {
                    return createTradeExecuted(record, message);
                }

                @Override
                public MessageProvider.Message.Builder visitUnknownValue(MessageProvider.EventType eventType) {
                    return null;
                }

                @Override
                public MessageProvider.Message.Builder visitMarketOrderRejected(MessageProvider.EventType eventType) {
                    return createMarketOrderRejected(record, message);
                }
            }.visit(evenType, evenType);
            return builder.build();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }


    }

    private MessageProvider.Message.Builder createMarketOrderRejected(GenericRecord record, MessageProvider.Message.Builder message) {
        GenericRecord marketOrderRejected = (GenericRecord) record.get("marketOrderRejected");
        GenericRecord time = (GenericRecord) marketOrderRejected.get("time");
        message.setMarketOrderRejected(MessageProvider.MarketOrderRejected.newBuilder()
                .setFillStatus(MessageProvider.FillStatus.valueOf(marketOrderRejected.get("fillStatus").toString()))
                .setTime(Timestamp.newBuilder()
                        .setSeconds(Long.parseLong(time.get("seconds").toString()))
                        .setNanos(Integer.parseInt(time.get("nanos").toString()))
                )
                .setId(marketOrderRejected.get("id").toString())
        );
        return message;
    }

    private MessageProvider.Message.Builder createTradeExecuted(GenericRecord record, MessageProvider.Message.Builder message) {
        GenericRecord tradeExecuted = (GenericRecord) record.get("tradeExecuted");
        GenericRecord time = (GenericRecord) tradeExecuted.get("time");
        message.setTradeExecuted(MessageProvider.TradeExecuted.newBuilder()
                .setBuyingLimit(Double.parseDouble(tradeExecuted.get("buyingLimit").toString()))
                .setBuyingBroker(tradeExecuted.get("buyingBroker").toString())
                .setQuantity(Integer.parseInt(tradeExecuted.get("quantity").toString()))
                .setSellingBroker(tradeExecuted.get("sellingBroker").toString())
                .setSellingLimit(Double.parseDouble(tradeExecuted.get("sellingLimit").toString()))
                .setTime(Timestamp.newBuilder()
                        .setSeconds(Long.parseLong(time.get("seconds").toString()))
                        .setNanos(Integer.parseInt(time.get("nanos").toString()))
                )
                .setPrice(Double.parseDouble(tradeExecuted.get("price").toString()))
                .setBuyingId(tradeExecuted.get("buyingId").toString())
                .setSellingId(tradeExecuted.get("sellingId").toString())
                .setSymbol(tradeExecuted.get("symbol").toString())
                .setBuyingOrderType(OrderType.valueOf(tradeExecuted.get("buyingOrderType").toString()))
                .setSellingOrderType(OrderType.valueOf(tradeExecuted.get("sellingOrderType").toString()))
        );
        return message;
    }

    private MessageProvider.Message.Builder createMarketOrderAccepted(GenericRecord record, MessageProvider.Message.Builder message) {
        GenericRecord marketOrderAccepted = (GenericRecord) record.get("marketOrderAccepted");
        GenericRecord time = (GenericRecord) marketOrderAccepted.get("time");
        message.setMarketOrderAccepted(MessageProvider.MarketOrderAccepted.newBuilder()
                .setSide(MessageProvider.Side.valueOf(marketOrderAccepted.get("side").toString()))
                .setBroker(marketOrderAccepted.get("broker").toString())
                .setQuantity(Integer.parseInt(marketOrderAccepted.get("quantity").toString()))
                .setTime(Timestamp.newBuilder()
                        .setSeconds(Long.parseLong(time.get("seconds").toString()))
                        .setNanos(Integer.parseInt(time.get("nanos").toString()))
                )
                .setId(marketOrderAccepted.get("id").toString())
                .setSymbol(marketOrderAccepted.get("symbol").toString())
        );
        return message;
    }

    private MessageProvider.Message.Builder createLimitOrderAccepted(GenericRecord record, MessageProvider.Message.Builder message) {
        GenericRecord limitOrderAccepted = (GenericRecord) record.get("limitOrderAccepted");
        GenericRecord time = (GenericRecord) limitOrderAccepted.get("time");
        message.setLimitOrderAccepted(MessageProvider.LimitOrderAccepted.newBuilder()
                .setSide(MessageProvider.Side.valueOf(limitOrderAccepted.get("side").toString()))
                .setBroker(limitOrderAccepted.get("broker").toString())
                .setQuantity(Integer.parseInt(limitOrderAccepted.get("quantity").toString()))
                .setTime(Timestamp.newBuilder()
                        .setSeconds(Long.parseLong(time.get("seconds").toString()))
                        .setNanos(Integer.parseInt(time.get("nanos").toString()))
                )
                .setId(limitOrderAccepted.get("id").toString())
                .setLimit(Double.parseDouble(limitOrderAccepted.get("limit").toString()))
                .setSymbol(limitOrderAccepted.get("symbol").toString())
        );
        return message;
    }
}
