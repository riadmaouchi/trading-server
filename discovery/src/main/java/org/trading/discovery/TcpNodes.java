package org.trading.discovery;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.trading.MessageProvider;
import org.trading.health.HealthCheckServer;
import org.trading.messaging.Message;
import org.trading.messaging.netty.TcpEventHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.slf4j.LoggerFactory.getLogger;

public class TcpNodes {
    private static final Logger LOGGER = getLogger(TcpNodes.class);
    private final Map<String, BatchEventProcessor<Message>> processors = new ConcurrentHashMap<>();
    private final Map<String, TcpEventHandler> handlers = new ConcurrentHashMap<>();
    private final Disruptor<Message> disruptor;
    private final Supplier<List<MessageProvider.Message>> supplier;
    private final HealthCheckServer healthCheckServer;
    private final ExecutorService executor = newCachedThreadPool(INSTANCE);

    public TcpNodes(Disruptor<Message> disruptor,
                    Supplier<List<MessageProvider.Message>> supplier,
                    HealthCheckServer healthCheckServer) {
        this.disruptor = disruptor;
        this.supplier = supplier;
        this.healthCheckServer = healthCheckServer;
    }

    public void setHealthyNodes(List<Node> healthyNodes) {
        processors.forEach((service, processor) -> processor.halt());

        handlers.forEach((service, handler) -> {
            try {
                handler.awaitShutdown();
            } catch (InterruptedException e) {
                LOGGER.warn("Thread Interrupted", e);
                Thread.currentThread().interrupt();
            }
        });

        handlers.clear();

        RingBuffer<Message> ringBuffer = disruptor.getRingBuffer();

        processors.forEach((key, value) -> ringBuffer.removeGatingSequence(value.getSequence()));
        processors.clear();

        for (Node service : healthyNodes) {
            TcpEventHandler handler = new TcpEventHandler(
                    service.address,
                    service.port,
                    service.id,
                    supplier,
                    healthCheckServer
            );
            BatchEventProcessor<Message> processor = new BatchEventProcessor<>(ringBuffer, ringBuffer.newBarrier(), handler);
            processors.put(service.id, processor);
            handlers.put(service.id, handler);
        }

        Sequence[] sequences = processors.values().stream()
                .map((BatchEventProcessor::getSequence))
                .toArray(Sequence[]::new);
        ringBuffer.addGatingSequences(sequences);

        processors.forEach((s, processor) -> executor.execute(processor));

    }
}
