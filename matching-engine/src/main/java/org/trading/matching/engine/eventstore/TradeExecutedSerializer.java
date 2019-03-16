package org.trading.matching.engine.eventstore;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.trading.api.message.OrderType;
import org.trading.eventstore.serializer.EventSerializer;
import org.trading.matching.engine.domain.OrderBook.TradeExecuted;
import org.trading.matching.engine.domain.Trade;

import java.time.LocalDateTime;
import java.util.UUID;

public class TradeExecutedSerializer extends EventSerializer<TradeExecuted> {

    private final Schema SCHEMA = getEventSchema("TradeExecuted")
            .name("buyingId").type().stringType().noDefault()
            .name("buyingBroker").type().stringType().noDefault()
            .name("sellingId").type().stringType().noDefault()
            .name("sellingBroker").type().stringType().noDefault()
            .name("quantity").type().intType().noDefault()
            .name("price").type().doubleType().noDefault()
            .name("buyingLimit").type().doubleType().noDefault()
            .name("sellingLimit").type().doubleType().noDefault()
            .name("time").type().stringType().noDefault()
            .name("symbol").type().stringType().noDefault()
            .name("buyingOrderType").type().stringType().noDefault()
            .name("sellingOrderType").type().stringType().noDefault()
            .endRecord();


    @Override
    public TradeExecuted deserialize(UUID id, long sequence, GenericRecord record) {
        return new TradeExecuted(id, sequence, new Trade(
                UUID.fromString(((Utf8) record.get("buyingId")).toString()),
                record.get("buyingBroker").toString(),
                UUID.fromString(((Utf8) record.get("sellingId")).toString()),
                record.get("sellingBroker").toString(),
                (int) record.get("quantity"),
                (double) record.get("price"),
                (double) record.get("buyingLimit"),
                (double) record.get("sellingLimit"),
                LocalDateTime.parse(record.get("time").toString()),
                record.get("symbol").toString(),
                OrderType.valueOf(record.get("buyingOrderType").toString()),
                OrderType.valueOf(record.get("sellingOrderType").toString())
        ));
    }

    @Override
    public void serialize(GenericData.Record record, TradeExecuted event) {
        record.put("buyingId", event.trade.buyingId.toString());
        record.put("buyingBroker", event.trade.buyingBroker);
        record.put("sellingId", event.trade.sellingId.toString());
        record.put("sellingBroker", event.trade.sellingBroker);
        record.put("quantity", event.trade.quantity);
        record.put("price", event.trade.price);
        record.put("buyingLimit", event.trade.buyingLimit);
        record.put("sellingLimit", event.trade.sellingLimit);
        record.put("time", event.trade.time.toString());
        record.put("symbol", event.trade.symbol);
        record.put("buyingOrderType", event.trade.buyingOrderType.name());
        record.put("sellingOrderType", event.trade.sellingOrderType.name());
    }

    @Override
    public Class getEventClass() {
        return TradeExecuted.class;
    }

    @Override
    public Schema getSchema() {
        return SCHEMA;
    }
}
