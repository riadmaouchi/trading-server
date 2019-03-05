package org.trading.web;

import org.slf4j.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.newSetFromMap;
import static org.slf4j.LoggerFactory.getLogger;

public class SseEventDispatcher {
    private static final Logger LOGGER = getLogger(SseEventDispatcher.class);
    private static final long TIMEOUT = -1;
    private final Set<AsyncContext> contexts = newSetFromMap(new ConcurrentHashMap<>());

    public void register(HttpServletRequest request, HttpServletResponse response) {
        setDefaultHeaders(response);
        final AsyncContext asyncContext = request.startAsync();
        contexts.add(asyncContext);
        asyncContext.setTimeout(TIMEOUT);
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onTimeout(AsyncEvent event) {
                LOGGER.info("Async dispatcher timeout on event: {}", event);
                contexts.remove(event.getAsyncContext());
                event.getAsyncContext().complete();
            }

            @Override
            public void onStartAsync(AsyncEvent event) {
                LOGGER.info("Async dispatcher register async on event: {}", event);
            }

            @Override
            public void onError(AsyncEvent event) {
                LOGGER.error("Async dispatcher error on event: {}", event);
                contexts.remove(event.getAsyncContext());
            }

            @Override
            public void onComplete(AsyncEvent event) {
                LOGGER.info("Async dispatcher complete on event: {}", event);
                contexts.remove(event.getAsyncContext());
            }
        });
        try {
            asyncContext.getResponse().flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setDefaultHeaders(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
    }

    public void dispatchEvent(String name, String data) {
        contexts.forEach(context -> dispatchEvent((HttpServletResponse) context.getResponse(), name, data));
    }

    private boolean dispatchEvent(HttpServletResponse response, String name, String data) {
        if (response == null) {
            return false;
        }

        try {
            PrintWriter writer = response.getWriter();
            if (writer.checkError()) {
                return false;
            }
            Optional.ofNullable(name).ifPresent(event -> writer.println("event: " + event));
            Optional.ofNullable(data).ifPresent(event -> writer.println("data: " + event));
            writer.println();

            return (!writer.checkError());
        } catch (IOException e) {
            return false;
        }
    }
}
