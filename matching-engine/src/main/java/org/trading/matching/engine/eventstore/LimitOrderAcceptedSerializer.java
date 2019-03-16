package org.trading.matching.engine.eventstore;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.trading.api.message.Side;
import org.trading.eventstore.serializer.EventSerializer;
import org.trading.matching.engine.domain.LimitOrder;
import org.trading.matching.engine.domain.OrderBook.LimitOrderAccepted;

import java.time.LocalDateTime;
import java.util.UUID;

public class LimitOrderAcceptedSerializer extends EventSerializer<LimitOrderAccepted> {

    private final Schema SCHEMA = getEventSchema("LimitOrderAccepted")
            .name("orderId").type().stringType().noDefault()
            .name("symbol").type().stringType().noDefault()
            .name("broker").type().stringType().noDefault()
            .name("quantity").type().intType().noDefault()
            .name("side").type().stringType().noDefault()
            .name("limit").type().doubleType().noDefault()
            .name("time").type().stringType().noDefault()
            .endRecord();


    @Override
    public Schema getSchema() {
        return SCHEMA;
    }

    @Override
    public LimitOrderAccepted deserialize(UUID id, long sequence, GenericRecord record) {
        return new LimitOrderAccepted(id, sequence, new LimitOrder(
                UUID.fromString(((Utf8) record.get("orderId")).toString()),
                record.get("symbol").toString(),
                record.get("broker").toString(),
                (int) record.get("quantity"),
                Side.valueOf(record.get("side").toString()),
                (double) record.get("limit"),
                LocalDateTime.parse(record.get("time").toString())
        ));
    }

    @Override
    public void serialize(GenericData.Record record, LimitOrderAccepted event) {
        record.put("orderId", event.limitOrder.id.toString());
        record.put("symbol", event.limitOrder.symbol);
        record.put("broker", event.limitOrder.broker);
        record.put("quantity", event.limitOrder.quantity);
        record.put("side", event.limitOrder.side.name());
        record.put("limit", event.limitOrder.limit);
        record.put("time", event.limitOrder.time.toString());
    }


    @Override
    public Class getEventClass() {
        return LimitOrderAccepted.class;
    }
}
