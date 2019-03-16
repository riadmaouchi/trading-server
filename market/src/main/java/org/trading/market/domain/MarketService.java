package org.trading.market.domain;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.trading.api.message.OrderType;
import org.trading.api.message.Side;
import org.trading.api.message.Side.SideVisitor;
import org.trading.market.command.LastTradePrice;
import org.trading.market.command.SubmitLimitOrder;
import org.trading.market.command.UpdateCurrencies;
import org.trading.market.command.UpdatePrecision;
import org.trading.market.event.OrderSubmitted;

import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

public class MarketService implements CommandListener {
    private static final Logger LOGGER = getLogger(MarketService.class);
    private final Map<String, Integer> precisions = new HashMap<>();
    private final Map<String, Double> ccyPairs = new HashMap<>();
    private final Map<String, Double> lastTradePrices = new HashMap<>();
    private final OrderEventListener orderEventListener;
    private final Random random = new Random();
    private final Faker faker = new Faker();
    private final List<Integer> quantities = List.of(5, 10, 15, 20);

    public MarketService(OrderEventListener orderEventListener) {
        this.orderEventListener = orderEventListener;
    }

    @Override
    public void onLastTradePrice(LastTradePrice lastTradePrice) {
        LOGGER.info(lastTradePrice.toString());
        lastTradePrices.put(lastTradePrice.symbol, lastTradePrice.price);
    }

    @Override
    public void onSubmitLimitOrder(SubmitLimitOrder submitLimitOrder) {

        if (ccyPairs.isEmpty()) {
            return;
        }

        List<String> symbols = new ArrayList<>(ccyPairs.keySet());
        String symbol = symbols.get(random.nextInt(symbols.size()));
        LOGGER.info(submitLimitOrder.toString());
        final Side side = random.nextBoolean() ? Side.BUY : Side.SELL;
        final double price1 = lastTradePrices.getOrDefault(symbol, ccyPairs.get(symbol));
        double randomDelta = new RandomNumberSupplier(-0.0020, 0.0020).getAsDouble();
        final double delta = side.accept(new SideVisitor<>() {
            @Override
            public Double visitBuy() {
                return price1 - price1 * randomDelta;
            }

            @Override
            public Double visitSell() {
                return price1 + price1 * randomDelta;
            }
        });

        double pow = Math.pow(10, precisions.getOrDefault(symbol, 5));
        final double price = Math.round(delta * pow) / pow;
        OrderSubmitted orderSubmitted = new OrderSubmitted(
                symbol,
                faker.company().name(),
                quantities.get(random.nextInt(quantities.size())) * Lot.STANDARD.numberOfUnits,
                side,
                OrderType.LIMIT,
                price
        );

        orderEventListener.onOrderSubmitted(orderSubmitted);
    }

    @Override
    public void updatePrecision(UpdatePrecision updatePrecision) {
        LOGGER.info(updatePrecision.toString());
        precisions.put(updatePrecision.symbol, updatePrecision.precision);
    }

    @Override
    public void updateCurrencies(UpdateCurrencies updateCurrencies) {
        ccyPairs.put(updateCurrencies.symbol, updateCurrencies.price);
    }
}
