package org.trading.matching.engine.domain;

import org.trading.api.command.Side;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class OrderBook {

    private final Deque<MarketOrder> marketOrderBook = new ArrayDeque<>();
    private final TreeSet<LimitOrder> limitOrderBook;

    public OrderBook(Side side) {
        limitOrderBook = new TreeSet<>(limitOrderComparator(side));
    }

    public boolean place(Order order) {
        return order.accept(new Order.OrderVisitor<>() {
            @Override
            public Boolean visitMarketOrder(MarketOrder marketOrder) {
                return marketOrderBook.offerLast(marketOrder);
            }

            @Override
            public Boolean visitLimitOrder(LimitOrder limitOrder) {
                return limitOrderBook.add(limitOrder);
            }
        });
    }

    Optional<Order> top() {
        return Stream.concat(marketOrderBook.stream(), limitOrderBook.stream()).findFirst();
    }

    public void decreaseTopBy(int quantity) {
        top().ifPresent(order -> {
            order.decreasedBy(quantity);
            order.accept(new Order.OrderVisitor<Void>() {
                @Override
                public Void visitMarketOrder(MarketOrder marketOrder) {
                    marketOrderBook.removeIf(Order::isClosed);
                    return null;
                }

                @Override
                public Void visitLimitOrder(LimitOrder limitOrder) {
                    limitOrderBook.removeIf(Order::isClosed);
                    return null;
                }
            });
        });
    }

    public List<Order> orders() {
        return Stream.concat(marketOrderBook.stream(), limitOrderBook.stream())
                .collect(toList());
    }

    private Comparator<LimitOrder> limitOrderComparator(Side side) {
        return side.accept(new Side.SideVisitor<>() {
            @Override
            public Comparator<LimitOrder> visitBuy() {
                return (o1, o2) -> o2.limit < o1.limit ? -1 : 1;
            }

            @Override
            public Comparator<LimitOrder> visitSell() {
                return (o1, o2) -> o1.limit < o2.limit ? -1 : 1;
            }
        });
    }

}
