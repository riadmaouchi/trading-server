package org.trading.eventstore.command;

import org.trading.eventstore.domain.Aggregate;

import java.util.UUID;

public interface Command<T extends Aggregate> {

    void execute(T t);

    UUID getId();

}
