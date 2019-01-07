package org.trading.api.command;

public enum OrderType {
    MARKET {
        @Override
        public <R> R accept(OrderTypeVisitor<R> visitor) {
            return visitor.visitMarket();
        }
    },
    LIMIT {
        @Override
        public <R> R accept(OrderTypeVisitor<R> visitor) {
            return visitor.visitLimit();
        }
    };

    public abstract <R> R accept(OrderTypeVisitor<R> visitor);

    public interface OrderTypeVisitor<R> {
        R visitMarket();

        R visitLimit();
    }

}
