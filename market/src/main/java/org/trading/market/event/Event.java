package org.trading.market.event;

import com.lmax.disruptor.EventFactory;

import java.util.StringJoiner;

public class Event {
    public EventType type;
    public Object event;

    public final static EventFactory<Event> FACTORY = Event::new;

    public enum EventType {
        ORDER_SUBMITTED {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitOrderSubmitted();
            }
        };

        public abstract <R> R accept(EventTypeVisitor<R> visitor);

        public interface EventTypeVisitor<R> {
            R visitOrderSubmitted();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Event.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("event=" + event)
                .toString();
    }
}
