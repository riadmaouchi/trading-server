package org.trading.messaging;

import com.lmax.disruptor.EventFactory;

import java.util.StringJoiner;

public final class Message {
    public EventType type;
    public Object event;

    public final static EventFactory<Message> FACTORY = Message::new;

    public enum EventType {
        SUBMIT_ORDER {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitSubmitOrder();
            }
        },
        SUBSCRIBE {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitSubscribe();
            }
        },
        MARKET_ORDER_ACCEPTED {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitMarketOrderAccepted();
            }
        },
        LIMIT_ORDER_ACCEPTED {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitLimitOrderAccepted();
            }
        },
        MARKET_ORDER_REJECTED {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitMarketOrderRejected();
            }
        },
        TRADE_EXECUTED {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitTradeExecuted();
            }
        },
        REQUEST_EXECUTION {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitRequestExecution();
            }
        },
        UPDATE_QUANTITIES {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitUpdateQuantities();
            }
        },
        ORDER_BOOK_CREATED {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitOrderBookCreated();
            }
        };

        public abstract <R> R accept(EventTypeVisitor<R> visitor);

        public interface EventTypeVisitor<R> {
            R visitSubmitOrder();

            R visitSubscribe();

            R visitMarketOrderAccepted();

            R visitLimitOrderAccepted();

            R visitMarketOrderRejected();

            R visitTradeExecuted();

            R visitRequestExecution();

            R visitUpdateQuantities();

            R visitOrderBookCreated();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("event=" + event)
                .toString();
    }
}
