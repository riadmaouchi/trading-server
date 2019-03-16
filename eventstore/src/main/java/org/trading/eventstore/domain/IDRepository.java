package org.trading.eventstore.domain;

import java.util.Optional;

public interface IDRepository<K,V> {

    void store(K key, V aggregateId);

    Optional<V> load(K key);
}
