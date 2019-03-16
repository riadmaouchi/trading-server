package org.trading.api.message;

public enum Side {
    BUY {
        @Override
        public <R> R accept(SideVisitor<R> visitor) {
            return visitor.visitBuy();
        }
    },
    SELL {
        @Override
        public <R> R accept(SideVisitor<R> visitor) {
            return visitor.visitSell();
        }
    };

    public abstract <R> R accept(SideVisitor<R> visitor);

    public interface SideVisitor<R> {
        R visitBuy();

        R visitSell();
    }
}
