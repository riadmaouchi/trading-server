package org.trading.matching.engine.domain;

import org.trading.api.message.FillStatus;
import org.trading.api.message.OrderType;
import org.trading.api.message.OrderType.OrderTypeVisitor;
import org.trading.api.message.Side;
import org.trading.api.message.Side.SideVisitor;
import org.trading.api.message.SubmitOrder;
import org.trading.eventstore.domain.Aggregate;
import org.trading.eventstore.domain.DomainEvent;
import org.trading.matching.engine.api.ExchangeResponseListener;
import org.trading.matching.engine.domain.Order.OrderVisitor;

import java.io.Closeable;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Supplier;

import static org.trading.api.message.FillStatus.FULLY_FILLED;
import static org.trading.api.message.FillStatus.PARTIALLY_FILLED;
import static org.trading.matching.engine.domain.LimitOrder.getBuyComparator;
import static org.trading.matching.engine.domain.LimitOrder.getSellComparator;

public final class OrderBook extends Aggregate implements Closeable {

    private final Clock clock;
    private final Supplier<UUID> idGenerator = UUID::randomUUID;
    private final TreeSet<LimitOrder> buyOrder = new TreeSet<>(getBuyComparator());
    private final TreeSet<LimitOrder> sellOrder = new TreeSet<>(getSellComparator());

    public OrderBook(Clock clock) {
        this.clock = clock;
    }

    public void createOrderBook(UUID id) {
        applyEvent(new OrderBookCreated(id, nextSequence()));
    }

    public void submitOrder(final SubmitOrder submitOrder) {

        final Order order = createOrder(submitOrder);


        order.accept(new OrderVisitor<Void>() {
            @Override
            public Void visitMarketOrder(MarketOrder marketOrder) {
                tryMatch(marketOrder);
                return null;
            }

            @Override
            public Void visitLimitOrder(LimitOrder limitOrder) {
                tryMatch(limitOrder);
                return null;
            }
        });
    }

    private Order createOrder(SubmitOrder submitOrder) {
        final UUID orderId = idGenerator.get();
        return submitOrder.orderType.accept(new OrderTypeVisitor<>() {
            private final LocalDateTime time = LocalDateTime.now(clock);

            @Override
            public Order visitMarket() {
                MarketOrder marketOrder = new MarketOrder(
                        orderId,
                        submitOrder.symbol,
                        submitOrder.broker,
                        submitOrder.amount,
                        submitOrder.side,
                        time
                );
                applyEvent(new MarketOrderAccepted(getId(), nextSequence(), marketOrder));
                return marketOrder;
            }

            @Override
            public Order visitLimit() {
                LimitOrder limitOrder = new LimitOrder(
                        orderId,
                        submitOrder.symbol,
                        submitOrder.broker,
                        submitOrder.amount,
                        submitOrder.side,
                        submitOrder.limit,
                        time
                );
                applyEvent(new LimitOrderAccepted(getId(), nextSequence(), limitOrder));
                return limitOrder;
            }
        });
    }

    private void tryMatch(LimitOrder limitOrder) {
        final Books books = getBooks(limitOrder.side);
        final TreeSet<LimitOrder> counterOrders = books.counterOrders;

        for (LimitOrder topOrder : new TreeSet<>(counterOrders)) {
            if (limitOrder.crossesAt(topOrder.limit)) {
                int matchedQuantity = Math.min(topOrder.getOpenQuantity(), limitOrder.getOpenQuantity());
                limitOrder.decreasedBy(matchedQuantity);
                applyEvent(new LimitOrderQuantityFilled(getId(), nextSequence(), topOrder, matchedQuantity));

                limitOrder.side.accept(new SideVisitor<Void>() {
                    @Override
                    public Void visitBuy() {
                        applyEvent(new TradeExecuted(
                                getId(),
                                nextSequence(),
                                new Trade(
                                        limitOrder.id,
                                        limitOrder.broker,
                                        topOrder.id,
                                        topOrder.broker,
                                        matchedQuantity,
                                        topOrder.limit,
                                        limitOrder.limit,
                                        topOrder.limit,
                                        LocalDateTime.now(clock),
                                        topOrder.symbol,
                                        OrderType.LIMIT,
                                        OrderType.LIMIT
                                )));
                        return null;
                    }

                    @Override
                    public Void visitSell() {
                        applyEvent(new TradeExecuted(
                                getId(),
                                nextSequence(),
                                new Trade(
                                        topOrder.id,
                                        topOrder.broker,
                                        limitOrder.id,
                                        limitOrder.broker,
                                        matchedQuantity,
                                        topOrder.limit,
                                        topOrder.limit,
                                        limitOrder.limit,
                                        LocalDateTime.now(clock),
                                        limitOrder.symbol,
                                        OrderType.LIMIT,
                                        OrderType.LIMIT
                                )));
                        return null;
                    }
                });
                if (topOrder.getOpenQuantity() == 0) {
                    //it.remove();
                    topOrder.side.accept(new SideVisitor<Void>() {
                        @Override
                        public Void visitBuy() {
                            applyEvent(new BuyLimitOrderFullyExecuted(getId(), nextSequence(), topOrder));
                            return null;
                        }

                        @Override
                        public Void visitSell() {
                            applyEvent(new SellLimitOrderFullyExecuted(getId(), nextSequence(), topOrder));
                            return null;
                        }
                    });
                }
                if (limitOrder.getOpenQuantity() == 0) {
                    break;
                }
            } else {
                break;
            }
        }

        if (limitOrder.getOpenQuantity() > 0) {
            limitOrder.side.accept(new SideVisitor<Void>() {
                @Override
                public Void visitBuy() {
                    applyEvent(new BuyLimitOrderPlaced(getId(), nextSequence(), limitOrder));
                    return null;
                }

                @Override
                public Void visitSell() {
                    applyEvent(new SellLimitOrderPlaced(getId(), nextSequence(), limitOrder));
                    return null;
                }
            });
        }
    }

    private void tryMatch(MarketOrder marketOrder) {
        final Books books = getBooks(marketOrder.side);
        final SortedSet<LimitOrder> counterOrders = books.counterOrders;

        for (LimitOrder topOrder : new TreeSet<>(counterOrders)) {
            if (marketOrder.crossesAt(topOrder.limit)) {
                int matchedQuantity = Math.min(topOrder.getOpenQuantity(), marketOrder.getOpenQuantity());
                marketOrder.decreasedBy(matchedQuantity);
                applyEvent(new LimitOrderQuantityFilled(getId(), nextSequence(), topOrder, matchedQuantity));

                marketOrder.side.accept(new SideVisitor<Void>() {
                    @Override
                    public Void visitBuy() {
                        applyEvent(new TradeExecuted(
                                getId(),
                                nextSequence(),
                                new Trade(
                                        marketOrder.id,
                                        marketOrder.broker,
                                        topOrder.id,
                                        topOrder.broker,
                                        matchedQuantity,
                                        topOrder.limit,
                                        0.,
                                        topOrder.limit,
                                        LocalDateTime.now(clock),
                                        topOrder.symbol,
                                        OrderType.MARKET,
                                        OrderType.LIMIT
                                )));
                        return null;
                    }

                    @Override
                    public Void visitSell() {
                        applyEvent(new TradeExecuted(
                                getId(),
                                nextSequence(),
                                new Trade(
                                        topOrder.id,
                                        topOrder.broker,
                                        marketOrder.id,
                                        marketOrder.broker,
                                        matchedQuantity,
                                        topOrder.limit,
                                        topOrder.limit,
                                        0.,
                                        LocalDateTime.now(clock),
                                        marketOrder.symbol,
                                        OrderType.LIMIT,
                                        OrderType.MARKET
                                )));
                        return null;
                    }
                });
                if (topOrder.getOpenQuantity() == 0) {
                    // it.remove();
                    topOrder.side.accept(new SideVisitor<Void>() {
                        @Override
                        public Void visitBuy() {
                            applyEvent(new BuyLimitOrderFullyExecuted(getId(), nextSequence(), topOrder));
                            return null;
                        }

                        @Override
                        public Void visitSell() {
                            applyEvent(new SellLimitOrderFullyExecuted(getId(), nextSequence(), topOrder));
                            return null;
                        }
                    });
                }
                if (marketOrder.getOpenQuantity() == 0) {
                    break;
                }
            } else {
                break;
            }
        }

        if (marketOrder.getOpenQuantity() > 0) {
            applyEvent(new MarketOrderRejected(
                    getId(),
                    nextSequence(),
                    marketOrder,
                    marketOrder.isClosed() ? FULLY_FILLED : PARTIALLY_FILLED
            ));
        }
    }

    private Books getBooks(Side side) {
        return side.accept(new SideVisitor<>() {

            private final Books sellCounterBooks = new Books(sellOrder, buyOrder);
            private final Books buyCounterBook = new Books(buyOrder, sellOrder);

            @Override
            public Books visitBuy() {
                return buyCounterBook;
            }

            @Override
            public Books visitSell() {
                return sellCounterBooks;
            }
        });
    }

    public List<LimitOrder> getBuyOrders() {
        return new ArrayList<>(buyOrder);
    }

    public List<LimitOrder> getSellOrders() {
        return new ArrayList<>(sellOrder);
    }

    @Override
    public void close() {

    }

    public static abstract class OrderDomainEvent extends DomainEvent<OrderBook> {

        public OrderDomainEvent(UUID id, long sequenceNumber) {
            super(id, sequenceNumber);
        }

        public abstract void accept(ExchangeResponseListener exchangeResponseListener);

    }

    private static class Books {
        final TreeSet<LimitOrder> orders;
        final TreeSet<LimitOrder> counterOrders;

        private Books(TreeSet<LimitOrder> orders, TreeSet<LimitOrder> counterOrders) {
            this.orders = orders;
            this.counterOrders = counterOrders;
        }
    }

    public static class OrderBookCreated extends OrderDomainEvent {

        public OrderBookCreated(UUID id, long sequenceNumber) {
            super(id, sequenceNumber);
        }

        @Override
        public void accept(ExchangeResponseListener listener) {
            listener.onOrderBookCreated(this);
        }

        @Override
        public void apply(OrderBook orderBook) {
            orderBook.setId(getAggregateId());
        }
    }

    public static class LimitOrderAccepted extends OrderDomainEvent {
        public final LimitOrder limitOrder;

        public LimitOrderAccepted(UUID id,
                                  long sequenceNumber,
                                  LimitOrder limitOrder) {
            super(id, sequenceNumber);
            this.limitOrder = limitOrder;
        }

        @Override
        public void apply(OrderBook orderBook) {
        }

        @Override
        public void accept(ExchangeResponseListener listener) {
            listener.onLimitOrderAccepted(this);
        }
    }

    public static class MarketOrderAccepted extends OrderDomainEvent {
        public final MarketOrder marketOrder;

        public MarketOrderAccepted(UUID id,
                                   long sequenceNumber,
                                   MarketOrder marketOrder) {
            super(id, sequenceNumber);
            this.marketOrder = marketOrder;
        }

        @Override
        public void apply(OrderBook orderBook) {

        }

        @Override
        public void accept(ExchangeResponseListener listener) {
            listener.onMarketOrderAccepted(this);
        }

    }

    public static class MarketOrderRejected extends OrderDomainEvent {
        public final MarketOrder marketOrder;
        public final FillStatus fillStatus;

        public MarketOrderRejected(UUID id,
                                   long sequenceNumber,
                                   MarketOrder marketOrder,
                                   FillStatus fillStatus) {
            super(id, sequenceNumber);
            this.marketOrder = marketOrder;
            this.fillStatus = fillStatus;
        }

        @Override
        public void apply(OrderBook orderBook) {
        }

        @Override
        public void accept(ExchangeResponseListener listener) {
            listener.onMarketOrderRejected(this);
        }

    }

    public static class BuyLimitOrderPlaced extends OrderDomainEvent {

        public final LimitOrder limitOrder;

        public BuyLimitOrderPlaced(UUID id,
                                   long sequenceNumber,
                                   LimitOrder limitOrder) {
            super(id, sequenceNumber);
            this.limitOrder = limitOrder;
        }

        @Override
        public void apply(OrderBook orderBook) {
            orderBook.buyOrder.add(limitOrder);
        }

        @Override
        public void accept(ExchangeResponseListener listener) {
            listener.onBuyLimitOrderPlaced(this);
        }

    }

    public static class SellLimitOrderPlaced extends OrderDomainEvent {

        public final LimitOrder limitOrder;

        public SellLimitOrderPlaced(UUID id,
                                    long sequenceNumber,
                                    LimitOrder limitOrder) {
            super(id, sequenceNumber);
            this.limitOrder = limitOrder;
        }

        @Override
        public void apply(OrderBook orderBook) {
            orderBook.sellOrder.add(limitOrder);
        }

        @Override
        public void accept(ExchangeResponseListener listener) {
            listener.onSellLimitOrderPlaced(this);
        }

    }

    public static class BuyLimitOrderFullyExecuted extends OrderDomainEvent {

        public final LimitOrder limitOrder;

        public BuyLimitOrderFullyExecuted(UUID id, long sequenceNumber, LimitOrder limitOrder) {
            super(id, sequenceNumber);
            this.limitOrder = limitOrder;
        }

        @Override
        public void apply(OrderBook orderBook) {
            orderBook.buyOrder.remove(limitOrder);
        }

        @Override
        public void accept(ExchangeResponseListener listener) {

        }

    }

    public static class SellLimitOrderFullyExecuted extends OrderDomainEvent {

        public final LimitOrder limitOrder;

        public SellLimitOrderFullyExecuted(UUID id, long sequenceNumber, LimitOrder limitOrder) {
            super(id, sequenceNumber);
            this.limitOrder = limitOrder;
        }

        @Override
        public void apply(OrderBook orderBook) {
            orderBook.sellOrder.remove(limitOrder);
        }

        @Override
        public void accept(ExchangeResponseListener listener) {

        }

    }

    public static class TradeExecuted extends OrderDomainEvent {
        public final Trade trade;

        public TradeExecuted(UUID id,
                             long sequenceNumber,
                             Trade trade) {
            super(id, sequenceNumber);
            this.trade = trade;
        }

        @Override
        public void apply(OrderBook orderBook) {
            //   orderBook.buyOrder.removeIf(Order::isClosed);
            //  orderBook.sellOrder.removeIf(Order::isClosed);
        }

        @Override
        public void accept(ExchangeResponseListener listener) {
            listener.onTradeExecuted(this);
        }

    }

    public static class MarketOrderQuantityFilled extends OrderDomainEvent {
        public final MarketOrder marketOrder;
        public final int quantity;

        public MarketOrderQuantityFilled(UUID id,
                                         long sequenceNumber,
                                         MarketOrder marketOrder,
                                         int quantity) {
            super(id, sequenceNumber);
            this.marketOrder = marketOrder;
            this.quantity = quantity;
        }

        @Override
        public void apply(OrderBook orderBook) {
            marketOrder.decreasedBy(quantity);
        }

        @Override
        public void accept(ExchangeResponseListener listener) {

        }

    }

    public static class LimitOrderQuantityFilled extends OrderDomainEvent {
        public final LimitOrder limitOrder;
        public final int quantity;

        public LimitOrderQuantityFilled(UUID id,
                                        long sequenceNumber,
                                        LimitOrder limitOrder,
                                        int quantity) {
            super(id, sequenceNumber);
            this.limitOrder = limitOrder;
            this.quantity = quantity;
        }

        @Override
        public void apply(OrderBook orderBook) {
            limitOrder.side.accept(new SideVisitor<Void>() {
                @Override
                public Void visitBuy() {
                    orderBook.buyOrder.first().decreasedBy(quantity);

                    return null;
                }

                @Override
                public Void visitSell() {
                    orderBook.sellOrder.first().decreasedBy(quantity);
                    return null;
                }
            });
        }

        @Override
        public void accept(ExchangeResponseListener listener) {

        }

    }
}
