package org.trading.matching.engine.eventstore;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.trading.api.message.Side;
import org.trading.eventstore.serializer.EventSerializer;
import org.trading.matching.engine.domain.MarketOrder;
import org.trading.matching.engine.domain.OrderBook.MarketOrderQuantityFilled;

import java.time.LocalDateTime;
import java.util.UUID;

public class MarketOrderQuantityFilledSerializer extends EventSerializer<MarketOrderQuantityFilled> {

    private final Schema SCHEMA = getEventSchema("MarketOrderQuantityFilled")
            .name("orderId").type().stringType().noDefault()
            .name("symbol").type().stringType().noDefault()
            .name("broker").type().stringType().noDefault()
            .name("quantity").type().intType().noDefault()
            .name("side").type().stringType().noDefault()
            .name("time").type().stringType().noDefault()
            .name("fillQuantity").type().intType().noDefault()
            .endRecord();

    @Override
    public MarketOrderQuantityFilled deserialize(UUID id, long sequence, GenericRecord record) {
        return new MarketOrderQuantityFilled(id, sequence,
                new MarketOrder(
                        UUID.fromString(((Utf8) record.get("orderId")).toString()),
                        record.get("symbol").toString(),
                        record.get("broker").toString(),
                        (int) record.get("quantity"),
                        Side.valueOf(record.get("side").toString()),
                        LocalDateTime.parse(record.get("time").toString())

                ),
                (int) record.get("fillQuantity"));
    }

    @Override
    public void serialize(GenericData.Record record, MarketOrderQuantityFilled event) {
        record.put("orderId", event.marketOrder.id.toString());
        record.put("symbol", event.marketOrder.symbol);
        record.put("broker", event.marketOrder.broker);
        record.put("quantity", event.marketOrder.quantity);
        record.put("side", event.marketOrder.side.name());
        record.put("time", event.marketOrder.time.toString());
        record.put("fillQuantity", event.quantity);
    }

    @Override
    public Class getEventClass() {
        return MarketOrderQuantityFilled.class;
    }

    @Override
    public Schema getSchema() {
        return SCHEMA;
    }


}
