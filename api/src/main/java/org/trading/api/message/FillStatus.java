package org.trading.api.message;

public enum FillStatus {
    FULLY_FILLED {
        @Override
        public <R> R accept(FillStatusVisitor<R> visitor) {
            return visitor.visitFullyFilled();
        }
    },
    PARTIALLY_FILLED {
        @Override
        public <R> R accept(FillStatusVisitor<R> visitor) {
            return visitor.visitPartiallyFilled();
        }
    };

    public abstract <R> R accept(FillStatusVisitor<R> visitor);

    public interface FillStatusVisitor<R> {
        R visitFullyFilled();

        R visitPartiallyFilled();
    }
}
