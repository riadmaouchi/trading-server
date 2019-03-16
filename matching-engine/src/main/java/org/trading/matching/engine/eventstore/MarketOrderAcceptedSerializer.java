package org.trading.matching.engine.eventstore;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.trading.api.message.Side;
import org.trading.eventstore.serializer.EventSerializer;
import org.trading.matching.engine.domain.MarketOrder;
import org.trading.matching.engine.domain.OrderBook.MarketOrderAccepted;

import java.time.LocalDateTime;
import java.util.UUID;

public class MarketOrderAcceptedSerializer extends EventSerializer<MarketOrderAccepted> {

    private final Schema SCHEMA = getEventSchema("MarketOrderAccepted")
            .name("orderId").type().stringType().noDefault()
            .name("symbol").type().stringType().noDefault()
            .name("broker").type().stringType().noDefault()
            .name("quantity").type().intType().noDefault()
            .name("side").type().stringType().noDefault()
            .name("time").type().stringType().noDefault()
            .endRecord();


    @Override
    public Schema getSchema() {
        return SCHEMA;
    }

    @Override
    public MarketOrderAccepted deserialize(UUID id, long sequence, GenericRecord record) {
        return new MarketOrderAccepted(id, sequence, new MarketOrder(
                UUID.fromString(((Utf8) record.get("orderId")).toString()),
                record.get("symbol").toString(),
                record.get("broker").toString(),
                (int) record.get("quantity"),
                Side.valueOf(record.get("side").toString()),
                LocalDateTime.parse(record.get("time").toString())
        ));
    }

    @Override
    public void serialize(GenericData.Record record, MarketOrderAccepted event) {
        record.put("orderId", event.marketOrder.id.toString());
        record.put("symbol", event.marketOrder.symbol);
        record.put("broker", event.marketOrder.broker);
        record.put("quantity", event.marketOrder.quantity);
        record.put("side", event.marketOrder.side.name());
        record.put("time", event.marketOrder.time.toString());
    }


    @Override
    public Class getEventClass() {
        return MarketOrderAccepted.class;
    }
}
