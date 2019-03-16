package org.trading.eventstore.command;

import org.trading.eventstore.domain.Aggregate;

public interface Processor<T extends Aggregate> {

    void process(Command<T> command);

    void processInitial(Command<T> command);
}
