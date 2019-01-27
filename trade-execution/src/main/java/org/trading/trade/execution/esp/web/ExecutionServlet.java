package org.trading.trade.execution.esp.web;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.TradeExecuted;
import org.trading.messaging.Message;
import org.trading.messaging.netty.TcpDataSource;
import org.trading.trade.execution.esp.HedgingEventHandler;
import org.trading.trade.execution.esp.ReportExecutionEventHandler;
import org.trading.trade.execution.esp.TradeMessage;
import org.trading.trade.execution.esp.TradeMessage.EventType.EventTypeVisitor;
import org.trading.trade.execution.esp.TradePublisher;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;
import org.trading.trade.execution.esp.domain.ExecutionRejected;
import org.trading.trade.execution.esp.domain.ExecutionRequest;
import org.trading.trade.execution.esp.domain.LastLook;
import org.trading.trade.execution.esp.domain.Trade;
import org.trading.trade.execution.esp.domain.TradeListener;
import org.trading.trade.execution.esp.translate.TradeTranslator;
import org.trading.trade.execution.esp.web.json.ExecutionAcceptedToJson;
import org.trading.trade.execution.esp.web.json.ExecutionRejectedToJson;
import org.trading.trade.execution.esp.web.json.ExecutionRequestFromJson;
import org.trading.web.SseEventDispatcher;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.lmax.disruptor.dsl.ProducerType.SINGLE;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static net.minidev.json.parser.JSONParser.MODE_RFC4627;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;
import static org.trading.messaging.Message.EventType.SUBSCRIBE;
import static org.trading.messaging.Message.FACTORY;

public class ExecutionServlet extends HttpServlet implements EventHandler<Message> {
    private final static Logger LOGGER = getLogger(ExecutionServlet.class);
    private final SseEventDispatcher eventDispatcher = new SseEventDispatcher();
    private final ExecutionRequestFromJson executionRequestFromJson = new ExecutionRequestFromJson();
    private final String host;
    private final int port;
    private Disruptor<Message> disruptor;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
    private final Map<String, AsyncContext> contexts = new ConcurrentHashMap<>();
    private LastLook lastLook;
    private final Disruptor<Message> executionDisruptor;

    public ExecutionServlet(String host, int port, Disruptor<Message> disruptor) {
        this.host = host;
        this.port = port;
        this.executionDisruptor = disruptor;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        disruptor = new Disruptor<>(FACTORY, 1024, INSTANCE, SINGLE, new BlockingWaitStrategy());
        disruptor.handleEventsWith(this);
        disruptor.start();
        TcpDataSource dataSource = new TcpDataSource(host, port, disruptor, "Execution");
        dataSource.connect();

        Disruptor<TradeMessage> outboundDisruptor = new Disruptor<>(
                TradeMessage.FACTORY,
                1024,
                INSTANCE,
                SINGLE,
                new BlockingWaitStrategy()
        );

        TradeListener tradeListener = new TradePublisher(outboundDisruptor);

        EventHandler<TradeMessage> executionReporter = new EventHandler<>() {

            private final ExecutionAcceptedToJson executionAcceptedToJson = new ExecutionAcceptedToJson();
            private final ExecutionRejectedToJson executionRejectedToJson = new ExecutionRejectedToJson();

            @Override
            public void onEvent(TradeMessage tradeMessage, long sequence, boolean endOfBatch) throws Exception {

                String id = tradeMessage.type.accept(new EventTypeVisitor<>() {
                    @Override
                    public String visitExecutionAccepted() {
                        ExecutionAccepted executionAccepted = (ExecutionAccepted) tradeMessage.event;
                        return executionAccepted.id;
                    }

                    @Override
                    public String visitExecutionRejected() {
                        ExecutionRejected executionRejected = (ExecutionRejected) tradeMessage.event;
                        return executionRejected.id;
                    }
                });
                String json = tradeMessage.type.accept(new EventTypeVisitor<>() {
                    @Override
                    public String visitExecutionAccepted() {
                        ExecutionAccepted executionAccepted = (ExecutionAccepted) tradeMessage.event;
                        return executionAcceptedToJson.toJson(executionAccepted).toJSONString();
                    }

                    @Override
                    public String visitExecutionRejected() {
                        ExecutionRejected executionRejected = (ExecutionRejected) tradeMessage.event;
                        return executionRejectedToJson.toJson(executionRejected).toJSONString();
                    }
                });
                AsyncContext context = contexts.get(id);
                if (context != null) {
                    LOGGER.info("context {}, ", context);
                    LOGGER.info("response {}", context.getResponse());
                    LOGGER.info("writer {}", context.getResponse().getWriter());
                    context.getResponse().getWriter().write(json);
                    HttpServletResponse resp = (HttpServletResponse) context.getResponse();
                    resp.setContentType(APPLICATION_JSON.asString());
                    resp.setStatus(SC_OK);
                    resp.setContentLength(json.length());
                    context.complete();
                }
            }
        };

        outboundDisruptor.handleEventsWith(
                new ReportExecutionEventHandler(eventDispatcher),
                new HedgingEventHandler(executionDisruptor),
                executionReporter

        );
        lastLook = new LastLook(tradeListener, 0.01);
        outboundDisruptor.start();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        eventDispatcher.register(req, resp);
        disruptor.publishEvent((event, sequence) -> event.type = SUBSCRIBE);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            String id = UUID.randomUUID().toString();
            AsyncContext asyncContext = req.startAsync(req, resp);
            asyncContext.setTimeout(2000);
            asyncContext.addListener(new AsyncListener() {
                @Override
                public void onComplete(AsyncEvent event) throws IOException {
                    contexts.remove(id);
                }

                @Override
                public void onTimeout(AsyncEvent event) throws IOException {
                    contexts.remove(id);
                }

                @Override
                public void onError(AsyncEvent event) throws IOException {
                    contexts.remove(id);
                }

                @Override
                public void onStartAsync(AsyncEvent event) throws IOException {
                }
            });
            contexts.put(id, asyncContext);
            JSONParser parser = new JSONParser(MODE_RFC4627);
            final JSONObject jsonObject = (JSONObject) parser.parse(req.getReader());
            LOGGER.info(jsonObject.toJSONString());
            ExecutionRequest executionRequest = executionRequestFromJson.toJson(jsonObject);
            Trade trade = new Trade(
                    id,
                    executionRequest.broker,
                    executionRequest.quantity,
                    executionRequest.side,
                    executionRequest.symbol,
                    executionRequest.price
            );
            executorService.submit(() -> {
                executorService.schedule(() -> disruptor.publishEvent(TradeTranslator::translateTo, trade), 300, MILLISECONDS);
            });
        } catch (ParseException e) {
            throw new ServletException("Invalid request", e);
        }
    }

    @Override
    public void onEvent(org.trading.messaging.Message message, long sequence, boolean endOfBatch) {
        LOGGER.info("Last look {}", message.toString());

        org.trading.messaging.Message.EventType eventType = message.type;
        switch (eventType) {
            case SUBSCRIBE:
                lastLook.reportExecutions();
                break;
            case LIMIT_ORDER_PLACED:
                LimitOrderPlaced limitOrderPlaced = ((LimitOrderPlaced) message.event);
                lastLook.onLimitOrderPlaced(limitOrderPlaced);
                break;
            case TRADE_EXECUTED:
                TradeExecuted tradeExecuted = ((TradeExecuted) message.event);
                lastLook.onTradeExecuted(tradeExecuted);
                break;
            case REQUEST_EXECUTION:
                lastLook.requestExecution((Trade) message.event);
                break;
            default:
                break;
        }

    }
}
