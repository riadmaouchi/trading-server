package org.trading.pricing.domain;

import it.unimi.dsi.fastutil.doubles.Double2IntAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntMap.Entry;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.trading.api.command.Side;
import org.trading.api.command.Side.SideVisitor;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.TradeExecuted;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.trading.api.command.Side.BUY;
import static org.trading.api.command.Side.SELL;

public final class PricingService {
    private static final int DEFAULT_PRECISION = 5;
    private final PriceListener priceListener;
    private List<Integer> quantities;
    private Map<String, Double> midPrices = new HashMap<>();

    private final Map<String, MarketDepth> depths = new HashMap<>();
    private static final Map<String, Integer> precisions = Map.of("EURUSD", 5, "EURGBP", 5, "EURJPY", 3);

    public PricingService(PriceListener priceListener, List<Integer> quantities) {
        this.priceListener = priceListener;
        this.quantities = quantities;
    }

    private void computePrice(String symbol) {
        MarketDepth marketDepth = depths.get(symbol);
        List<Price> buyPrice = computePrice(marketDepth.orders(BUY), precisions.getOrDefault(symbol, DEFAULT_PRECISION));
        List<Price> sellPrice = computePrice(marketDepth.orders(SELL), precisions.getOrDefault(symbol, DEFAULT_PRECISION));
        Prices price = new Prices(
                symbol,
                LocalDateTime.now(),
                buyPrice,
                sellPrice,
                midPrices.get(symbol)
        );
        marketDepth.lastPrices = price;
        priceListener.onPrices(price);
    }

    private void computePrice(String symbol, Side side) {
        MarketDepth marketDepth = depths.get(symbol);
        List<Price> price = computePrice(marketDepth.orders(side), precisions.getOrDefault(symbol, DEFAULT_PRECISION));
        final LocalDateTime time = LocalDateTime.now();

        List<Price> bids = new ArrayList<>(quantities.size());
        List<Price> asks = new ArrayList<>(quantities.size());

        Prices lastPrices = ofNullable(marketDepth.lastPrices)
                .orElseGet(() -> new Prices(symbol, time, bids, asks,  midPrices.get(symbol)));

        Prices prices = side.accept(new SideVisitor<>() {

            @Override
            public Prices visitBuy() {
                return new Prices(symbol, time, price, lastPrices.asks,  midPrices.get(symbol));
            }

            @Override
            public Prices visitSell() {
                return new Prices(symbol, time, lastPrices.bids, price,  midPrices.get(symbol));
            }
        });
        if (!prices.asks.isEmpty() || !prices.bids.isEmpty()) {
            marketDepth.lastPrices = prices;
            priceListener.onPrices(prices);
        }
    }

    private List<Price> computePrice(Double2IntAVLTreeMap orders, int precision) {
        final List<Price> prices = new ArrayList<>();
        quantities.stream()
                .filter(quantity -> quantity <= orders.values().stream()
                        .mapToInt(Number::intValue)
                        .sum())
                .forEach((IntConsumer) quantity -> prices.add(new Price(quantity, computePrice(quantity, orders, precision))));
        return prices;
    }

    private double computePrice(double bucketSize, Double2IntMap orderBook, int precision) {
        final ObjectIterator<Entry> it = orderBook.double2IntEntrySet().iterator();
        double totalVolume = 0.;
        double averagePrice = 0.;
        while (it.hasNext() && (totalVolume < bucketSize)) {
            final Entry order = it.next();
            final double liquidity = order.getIntValue();
            final double price = order.getDoubleKey();
            double quantity = bucketSize - totalVolume;
            totalVolume += liquidity;
            averagePrice += price * (totalVolume < bucketSize ? liquidity : quantity) / bucketSize;
        }
        double pow = Math.pow(10, precision);
        return Math.round(averagePrice * pow) / pow;
    }

    public void onLimitOrderPlaced(LimitOrderPlaced limitOrderPlaced) {
        midPrices.putIfAbsent(limitOrderPlaced.symbol, limitOrderPlaced.price);

        depths.computeIfAbsent(limitOrderPlaced.symbol, symbol -> new MarketDepth())
                .orders(limitOrderPlaced.side)
                .merge(
                        limitOrderPlaced.price,
                        limitOrderPlaced.quantity,
                        Integer::sum
                );
        computePrice(limitOrderPlaced.symbol, limitOrderPlaced.side);
    }

    public void onTradeExecuted(TradeExecuted tradeExecuted) {
        depths.get(tradeExecuted.symbol).buyOrders.computeIfPresent(
                tradeExecuted.buyingLimit,
                (price, quantity) -> quantity - tradeExecuted.quantity
        );

        depths.get(tradeExecuted.symbol).sellOrders.computeIfPresent(
                tradeExecuted.sellingLimit,
                (price, quantity) -> quantity - tradeExecuted.quantity
        );
        midPrices.put(tradeExecuted.symbol, tradeExecuted.price);
        computePrice(tradeExecuted.symbol);
    }

    public Collection<Prices> getLastPrices() {
        return depths.values().stream()
                .map(marketDepth -> marketDepth.lastPrices)
                .filter(value -> ofNullable(value).isPresent())
                .collect(toList());
    }

    public void updateQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }

    private class MarketDepth {
        private final Double2IntAVLTreeMap buyOrders = new Double2IntAVLTreeMap(Comparator.reverseOrder());
        private final Double2IntAVLTreeMap sellOrders = new Double2IntAVLTreeMap(Double::compareTo);
        private Prices lastPrices;

        Double2IntAVLTreeMap orders(Side side) {
            return side.accept(new SideVisitor<>() {
                @Override
                public Double2IntAVLTreeMap visitBuy() {
                    return buyOrders;
                }

                @Override
                public Double2IntAVLTreeMap visitSell() {
                    return sellOrders;
                }
            });
        }
    }
}
