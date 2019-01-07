package org.trading.trade.execution.order.web;

import com.lmax.disruptor.dsl.Disruptor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.trading.messaging.Message;
import org.trading.trade.execution.order.web.json.SubmitOrderFromJson;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static net.minidev.json.parser.JSONParser.MODE_RFC4627;

public class OrderServlet extends HttpServlet {

    private final SubmitOrderFromJson submitOrderFromJson = new SubmitOrderFromJson();
    private final Disruptor<Message> disruptor;

    public OrderServlet(Disruptor<Message> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            JSONParser parser = new JSONParser(MODE_RFC4627);
            final JSONObject jsonObject = (JSONObject) parser.parse(req.getReader());
            disruptor.publishEvent((event, sequence) -> {
                event.event = submitOrderFromJson.fromJson(jsonObject);
                event.type = Message.EventType.SUBMIT_ORDER;
            });
            resp.setContentType("application/json");
            resp.setStatus(SC_OK);
        } catch (ParseException e) {
            throw new ServletException("Invalid request", e);
        }
    }
}
