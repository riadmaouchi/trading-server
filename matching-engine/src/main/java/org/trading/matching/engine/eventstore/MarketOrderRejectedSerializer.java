package org.trading.matching.engine.eventstore;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.trading.api.message.FillStatus;
import org.trading.api.message.Side;
import org.trading.eventstore.serializer.EventSerializer;
import org.trading.matching.engine.domain.MarketOrder;
import org.trading.matching.engine.domain.OrderBook.MarketOrderRejected;

import java.time.LocalDateTime;
import java.util.UUID;

public class MarketOrderRejectedSerializer extends EventSerializer<MarketOrderRejected> {

    private final Schema SCHEMA = getEventSchema("MarketOrderRejected")
            .name("orderId").type().stringType().noDefault()
            .name("symbol").type().stringType().noDefault()
            .name("broker").type().stringType().noDefault()
            .name("quantity").type().intType().noDefault()
            .name("side").type().stringType().noDefault()
            .name("time").type().stringType().noDefault()
            .name("fillStatus").type().stringType().noDefault()
            .endRecord();


    @Override
    public Schema getSchema() {
        return SCHEMA;
    }

    @Override
    public MarketOrderRejected deserialize(UUID id, long sequence, GenericRecord record) {
        return new MarketOrderRejected(id, sequence, new MarketOrder(
                UUID.fromString(((Utf8) record.get("orderId")).toString()),
                record.get("symbol").toString(),
                record.get("broker").toString(),
                (int) record.get("quantity"),
                Side.valueOf(record.get("side").toString()),
                LocalDateTime.parse(record.get("time").toString())
        ), FillStatus.valueOf(record.get("fillStatus").toString()));
    }

    @Override
    public void serialize(GenericData.Record record, MarketOrderRejected event) {
        record.put("orderId", event.marketOrder.id.toString());
        record.put("symbol", event.marketOrder.symbol);
        record.put("broker", event.marketOrder.broker);
        record.put("quantity", event.marketOrder.quantity);
        record.put("side", event.marketOrder.side.name());
        record.put("time", event.marketOrder.time.toString());
        record.put("fillStatus", event.fillStatus.name());
    }


    @Override
    public Class getEventClass() {
        return MarketOrderRejected.class;
    }
}
