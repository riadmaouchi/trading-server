package org.trading.eventstore.domain;

import java.util.Optional;
import java.util.UUID;

public interface AggregateRepository<T extends Aggregate> {

    Optional<T> load(UUID id);

    T create();

    void save(T t);
}
