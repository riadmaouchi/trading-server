package org.trading.matching.engine.eventstore;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.trading.eventstore.serializer.EventSerializer;
import org.trading.matching.engine.domain.OrderBook.OrderBookCreated;

import java.util.UUID;

public class OrderBookCreatedSerializer extends EventSerializer<OrderBookCreated> {

    private final Schema SCHEMA = getEventSchema("OrderBookCreated")
            .endRecord();

    @Override
    public OrderBookCreated deserialize(UUID id, long sequence, GenericRecord record) {
        return new OrderBookCreated(id, sequence);
    }

    @Override
    public void serialize(GenericData.Record record, OrderBookCreated event) {

    }

    @Override
    public Class getEventClass() {
        return OrderBookCreated.class;
    }

    @Override
    public Schema getSchema() {
        return SCHEMA;
    }
}
