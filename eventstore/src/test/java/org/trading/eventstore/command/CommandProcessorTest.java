package org.trading.eventstore.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.eventstore.domain.Aggregate;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CommandProcessorTest {

    private CommandProcessor<Aggregate> processor;
    private Command<Aggregate> command;
    private Supplier initialAggregateSupplier;
    private Aggregate aggregate;
    private Function aggregateSupplier;
    private Consumer aggregateConsumer;

    @BeforeEach
    void before() {
        initialAggregateSupplier = mock(Supplier.class);
        aggregate = new Aggregate() {
        };
        given(initialAggregateSupplier.get()).willReturn(aggregate);

        command = mock(Command.class);

        aggregateSupplier = mock(Function.class);
        given(aggregateSupplier.apply(command.getId())).willReturn(Optional.of(aggregate));
        aggregateConsumer = mock(Consumer.class);
        processor = new CommandProcessor<Aggregate>(
                aggregateSupplier,
                initialAggregateSupplier,
                aggregateConsumer
        );
        command = mock(Command.class);
    }

    @Test
    void should_process_initial_command() {

        // When
        processor.processInitial(command);

        // Then
        verify(initialAggregateSupplier).get();
        verify(command).execute(aggregate);
        verify(aggregateConsumer).accept(aggregate);
        verify(aggregateSupplier, never()).apply(command.getId());
    }

    @Test
    void should_process_command() {

        // When
        processor.process(command);

        // Then
        verify(initialAggregateSupplier, never()).get();
        verify(command).execute(aggregate);
        verify(aggregateConsumer).accept(aggregate);
        verify(aggregateSupplier).apply(command.getId());
    }

}