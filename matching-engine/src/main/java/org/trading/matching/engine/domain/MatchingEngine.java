package org.trading.matching.engine.domain;

import org.trading.api.command.OrderType;
import org.trading.api.command.Side;
import org.trading.api.command.Side.SideVisitor;
import org.trading.api.command.SubmitOrder;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.MarketOrderPlaced;
import org.trading.api.event.TradeExecuted;
import org.trading.api.service.OrderListener;
import org.trading.api.service.OrderEventListener;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.trading.api.command.Side.BUY;
import static org.trading.api.command.Side.SELL;

public final class MatchingEngine implements OrderListener {

    private final OrderBook buyOrderBook = new OrderBook(BUY);
    private final OrderBook sellOrderBook = new OrderBook(SELL);
    private final OrderEventListener orderEventListener;
    private final Clock clock;
    private final Supplier<UUID> idGenerator = UUID::randomUUID;

    public MatchingEngine(OrderEventListener orderEventListener,
                          Clock clock) {
        this.orderEventListener = orderEventListener;
        this.clock = clock;
    }

    @Override
    public void submitOrder(final SubmitOrder submitOrder) {
        final Order order = createOrder(submitOrder);
        final Books books = getBooks(order.side);
        final OrderBook counterOrderBook = books.counterOrderBook;
        tryMatch(order, counterOrderBook).ifPresent(unfilledOrder -> getBooks(unfilledOrder.side).orderBook.place(unfilledOrder));
    }

    private Order createOrder(SubmitOrder submitOrder) {
        final UUID id = idGenerator.get();
        return submitOrder.orderType.accept(new OrderType.OrderTypeVisitor<>() {
            private final LocalDateTime time = LocalDateTime.now(clock);

            @Override
            public Order visitMarket() {
                orderEventListener.onMarketOrderPlaced(new MarketOrderPlaced(
                        id,
                        submitOrder.broker,
                        submitOrder.amount,
                        submitOrder.side,
                        submitOrder.symbol,
                        time
                ));
                return new MarketOrder(
                        id,
                        submitOrder.symbol,
                        submitOrder.broker,
                        submitOrder.amount,
                        submitOrder.side,
                        time
                );
            }

            @Override
            public Order visitLimit() {
                orderEventListener.onLimitOrderPlaced(new LimitOrderPlaced(
                        id,
                        time,
                        submitOrder.broker,
                        submitOrder.amount,
                        submitOrder.side,
                        submitOrder.limit,
                        submitOrder.symbol
                ));
                return new LimitOrder(
                        id,
                        submitOrder.symbol,
                        submitOrder.broker,
                        submitOrder.amount,
                        submitOrder.side,
                        submitOrder.limit,
                        time
                );
            }
        });
    }

    private Optional<Order> tryMatch(Order order, OrderBook counterOrderBook) {

        if (order.isClosed()) {
            return Optional.empty();
        }

        return counterOrderBook.top().map((Order topOrder) -> topOrder.accept(new Order.OrderVisitor<Optional<Order>>() {
            @Override
            public Optional<Order> visitMarketOrder(MarketOrder marketOrder) {
                return order.accept(new Order.OrderVisitor<>() {
                    @Override
                    public Optional<Order> visitMarketOrder(MarketOrder marketOrder) {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<Order> visitLimitOrder(LimitOrder limitOrder) {
                        return tryMatchWithTop(limitOrder, marketOrder, limitOrder.limit).map(trade -> {
                            counterOrderBook.decreaseTopBy(trade.quantity);
                            publishTrade(trade);
                            limitOrder.decreasedBy(trade.quantity);
                            return tryMatch(limitOrder, counterOrderBook);
                        }).filter(Optional::isEmpty).orElse(Optional.of(limitOrder));
                    }
                });
            }

            @Override
            public Optional<Order> visitLimitOrder(LimitOrder limitOrder) {
                return tryMatchWithTop(order, limitOrder, limitOrder.limit).map(trade -> {
                    counterOrderBook.decreaseTopBy(trade.quantity);
                    publishTrade(trade);
                    order.decreasedBy(trade.quantity);
                    return tryMatch(order, counterOrderBook);
                }).filter(Optional::isEmpty).orElse(Optional.of(order));
            }
        })).orElseGet(() -> Optional.of(order));

    }

    private void publishTrade(TradeExecuted tradeExecuted) {
        orderEventListener.onTradeExecuted(tradeExecuted);
    }

    private Optional<TradeExecuted> tryMatchWithTop(Order order, Order topOrder, double limit) {

        if (order.crossesAt(limit)) {
            final int quantity = Math.min(topOrder.getOpenQuantity(), order.getOpenQuantity());

            final TradeExecuted tradeExecuted = order.side.accept(new SideVisitor<>() {
                @Override
                public TradeExecuted visitBuy() {
                    return new TradeExecuted(
                            order.id,
                            order.broker,
                            topOrder.id,
                            topOrder.broker,
                            quantity,
                            limit,
                            computeLimitPrice(order),
                            computeLimitPrice(topOrder),
                            LocalDateTime.now(clock),
                            order.symbol
                    );
                }

                @Override
                public TradeExecuted visitSell() {
                    return new TradeExecuted(
                            topOrder.id,
                            topOrder.broker,
                            order.id,
                            order.broker,
                            quantity,
                            limit,
                            computeLimitPrice(topOrder),
                            computeLimitPrice(order),
                            LocalDateTime.now(clock),
                            order.symbol);
                }
            });
            return Optional.of(tradeExecuted);

        }
        return Optional.empty();

    }

    private double computeLimitPrice(Order order) {
        return order.accept(new Order.OrderVisitor<>() {
            @Override
            public Double visitMarketOrder(MarketOrder marketOrder) {
                return 0.;
            }

            @Override
            public Double visitLimitOrder(LimitOrder limitOrder) {
                return limitOrder.limit;
            }
        });
    }

    private Books getBooks(Side side) {
        return side.accept(new SideVisitor<>() {
            @Override
            public Books visitBuy() {
                return new Books(buyOrderBook, sellOrderBook);
            }

            @Override
            public Books visitSell() {
                return new Books(sellOrderBook, buyOrderBook);
            }
        });
    }

    public List<Order> getBuyOrderBook() {
        return buyOrderBook.orders();
    }

    public List<Order> getSellOrderBook() {
        return sellOrderBook.orders();
    }

    private static class Books {
        final OrderBook orderBook;
        final OrderBook counterOrderBook;

        private Books(OrderBook orderBook, OrderBook counterOrderBook) {
            this.orderBook = orderBook;
            this.counterOrderBook = counterOrderBook;
        }
    }
}
