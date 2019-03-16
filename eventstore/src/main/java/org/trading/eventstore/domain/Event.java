package org.trading.eventstore.domain;

import java.util.UUID;

public interface Event {

    UUID getAggregateId();

    long getSequence();

}
