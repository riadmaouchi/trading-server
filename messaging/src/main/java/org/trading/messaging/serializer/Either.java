package org.trading.messaging.serializer;

import java.util.function.Consumer;

public final class Either<A, B> {
    private final A left;
    private final B right;

    private Either(A a, B b) {
        left = a;
        right = b;
    }

    public static <A, B> Either<A, B> left(A a) {
        return new Either<>(a, null);
    }

    public A left() {
        return left;
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    public B right() {
        return right;
    }

    public static <A, B> Either<A, B> right(B b) {
        return new Either<>(null, b);
    }

    public void fold(Consumer<A> leftOption, Consumer<B> rightOption) {
        if (right == null)
            leftOption.accept(left);
        else
            rightOption.accept(right);
    }
}
