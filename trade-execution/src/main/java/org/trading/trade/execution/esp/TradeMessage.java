package org.trading.trade.execution.esp;

import com.google.common.base.MoreObjects;
import com.lmax.disruptor.EventFactory;

public class TradeMessage {
    public EventType type;
    public Object event;

    public final static EventFactory<TradeMessage> FACTORY = TradeMessage::new;

    public enum EventType {

        EXECUTION_ACCEPTED {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitExecutionAccepted();
            }
        },
        EXECUTION_REJECTED {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitExecutionRejected();
            }
        };

        public abstract <R> R accept(EventTypeVisitor<R> visitor);

        public interface EventTypeVisitor<R> {
            R visitExecutionAccepted();

            R visitExecutionRejected();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("event", event)
                .toString();
    }
}
