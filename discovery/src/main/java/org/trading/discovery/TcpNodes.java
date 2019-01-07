package org.trading.discovery;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.dsl.Disruptor;
import com.orbitz.consul.model.health.Service;
import org.trading.messaging.Message;
import org.trading.messaging.netty.TcpEventHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class TcpNodes {
    private final Map<String, BatchEventProcessor<Message>> processors = new ConcurrentHashMap<>();
    private final Map<String, TcpEventHandler> handlers = new ConcurrentHashMap<>();
    private final Disruptor<Message> disruptor;
    private final ExecutorService executor = newCachedThreadPool(INSTANCE);

    public TcpNodes(Disruptor<Message> disruptor) {
        this.disruptor = disruptor;
    }

    public void setHealthyNodes(List<Node> healthyNodes) {
        processors.forEach((service, processor) -> processor.halt());

        handlers.forEach((service, handler) -> {
            try {
                handler.awaitShutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                    service.id
            );
            BatchEventProcessor<Message> processor = new BatchEventProcessor<>(ringBuffer, ringBuffer.newBarrier(), handler);
            processors.put(service.id, processor);
            handlers.put(service.id, handler);
        }

        Sequence[] sequences = processors.entrySet().stream()
                .map((node -> node.getValue().getSequence()))
                .toArray(Sequence[]::new);
        ringBuffer.addGatingSequences(sequences);

        processors.forEach((s, processor) -> executor.execute(processor));

    }
}
