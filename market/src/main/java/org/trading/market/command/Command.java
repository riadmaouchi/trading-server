package org.trading.market.command;

import com.lmax.disruptor.EventFactory;

import java.util.StringJoiner;

public class Command {
    public EventType type;
    public Object event;

    public final static EventFactory<Command> FACTORY = Command::new;

    public enum EventType {
        SUBMIT_LIMIT_ORDER {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitSubmitLimitOrder();
            }
        },
        LAST_TRADE_PRICE {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitLastTradePrice();
            }
        },
        UPDATE_PRECISION {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitUpdatePrecision();
            }
        },
        UPDATE_CURRENCIES {
            @Override
            public <R> R accept(EventTypeVisitor<R> visitor) {
                return visitor.visitUpdateCurrencies();
            }
        };

        public abstract <R> R accept(EventTypeVisitor<R> visitor);

        public interface EventTypeVisitor<R> {
            R visitSubmitLimitOrder();

            R visitLastTradePrice();

            R visitUpdatePrecision();

            R visitUpdateCurrencies();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Command.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("event=" + event)
                .toString();
    }
}
