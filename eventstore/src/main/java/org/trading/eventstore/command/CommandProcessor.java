package org.trading.eventstore.command;

import org.slf4j.Logger;
import org.trading.eventstore.domain.Aggregate;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

public class CommandProcessor<T extends Aggregate> implements Processor {
    private static final Logger LOGGER = getLogger(CommandProcessor.class);
    private final Function<UUID, Optional<T>> aggregateSupplier;
    private final Supplier<T> initialAggregateSupplier;
    private final Consumer<T> aggregateConsumer;

    public CommandProcessor(Function<UUID, Optional<T>> aggregateSupplier,
                            Supplier<T> initialAggregateSupplier,
                            Consumer<T> aggregateConsumer) {
        this.aggregateSupplier = aggregateSupplier;
        this.initialAggregateSupplier = initialAggregateSupplier;
        this.aggregateConsumer = aggregateConsumer;
    }

    @Override
    public void process(Command command) {
        aggregateSupplier.apply(command.getId())
                .ifPresentOrElse(
                        t -> process(command, t), () -> LOGGER.warn("Unknown aggregate for ID {}", command.getId())
                );
    }

    @Override
    public void processInitial(Command command) {
        T t = initialAggregateSupplier.get();
        process(command, t);
    }

    private void process(Command command, T t) {
        command.execute(t);
        aggregateConsumer.accept(t);
    }
}
