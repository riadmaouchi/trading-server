package org.trading.matching.engine.eventstore;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.trading.api.message.Side;
import org.trading.eventstore.serializer.EventSerializer;
import org.trading.matching.engine.domain.LimitOrder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.trading.matching.engine.domain.OrderBook.LimitOrderQuantityFilled;

public class LimitOrderQuantityFilledSerializer extends EventSerializer<LimitOrderQuantityFilled> {

    private final Schema SCHEMA = getEventSchema("LimitOrderQuantityFilled")
            .name("orderId").type().stringType().noDefault()
            .name("symbol").type().stringType().noDefault()
            .name("broker").type().stringType().noDefault()
            .name("quantity").type().intType().noDefault()
            .name("side").type().stringType().noDefault()
            .name("limit").type().doubleType().noDefault()
            .name("time").type().stringType().noDefault()
            .name("fillQuantity").type().intType().noDefault()
            .endRecord();

    @Override
    public LimitOrderQuantityFilled deserialize(UUID id, long sequence, GenericRecord record) {
        return new LimitOrderQuantityFilled(id, sequence,
                new LimitOrder(
                        UUID.fromString(((Utf8) record.get("orderId")).toString()),
                        record.get("symbol").toString(),
                        record.get("broker").toString(),
                        (int) record.get("quantity"),
                        Side.valueOf(record.get("side").toString()),
                        (double) record.get("limit"),
                        LocalDateTime.parse(record.get("time").toString())

                ),
                (int) record.get("fillQuantity"));
    }

    @Override
    public void serialize(GenericData.Record record, LimitOrderQuantityFilled limitOrderQuantityFilled) {
        record.put("orderId", limitOrderQuantityFilled.limitOrder.id.toString());
        record.put("symbol", limitOrderQuantityFilled.limitOrder.symbol);
        record.put("broker", limitOrderQuantityFilled.limitOrder.broker);
        record.put("quantity", limitOrderQuantityFilled.limitOrder.quantity);
        record.put("side", limitOrderQuantityFilled.limitOrder.side.name());
        record.put("limit", limitOrderQuantityFilled.limitOrder.limit);
        record.put("time", limitOrderQuantityFilled.limitOrder.time.toString());
        record.put("fillQuantity", limitOrderQuantityFilled.quantity);
    }

    @Override
    public Class getEventClass() {
        return LimitOrderQuantityFilled.class;
    }

    @Override
    public Schema getSchema() {
        return SCHEMA;
    }


}
