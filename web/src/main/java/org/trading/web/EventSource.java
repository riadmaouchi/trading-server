package org.trading.web;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.Listener.Adapter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static net.minidev.json.parser.JSONParser.MODE_RFC4627;
import static org.eclipse.jetty.http.HttpMethod.GET;

public class EventSource {

    private final JSONParser parser = new JSONParser(MODE_RFC4627);
    private final String url;
    private final String path;

    public EventSource(String url, String path) {
        this.url = url;
        this.path = path;
    }

    public void addEventListener(EventHandler... eventHandlers) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
            newSingleThreadExecutor().submit(() -> httpClient.newRequest(url)
                    .path(path)
                    .method(GET)
                    .send(new Adapter() {
                        @Override
                        public void onContent(Response response, ByteBuffer buffer) {
                            Event.Builder builder = Event.Builder.newBuilder();
                            while (buffer.remaining() != 0) {
                                int b = buffer.get();

                                if (b == '\n') {
                                    byte[] bytes = outputStream.toByteArray();
                                    outputStream.reset();
                                    String message = new String(bytes, UTF_8);
                                    StringBuilder comment;
                                    if (message.startsWith("data:")) {
                                        builder.withData(message.substring(5).trim());
                                    } else if (message.startsWith("id:")) {
                                        builder.withId(message.substring(3));
                                    } else if (message.startsWith("event:")) {
                                        builder.withEvent(message.substring(6).trim());
                                    } else if (message.startsWith("retry:")) {
                                        builder.withReconnectTime((Long.valueOf(message.substring(6))));
                                    } else if (message.startsWith(":")) {
                                        comment = new StringBuilder();
                                        comment.append(message.substring(1)).append("\n");
                                    }
                                } else {
                                    outputStream.write(b);
                                }
                            }
                            Event event = builder.build();
                            try {
                                JSONObject jsonObject = (JSONObject) parser.parse(event.data);
                                Stream.of(eventHandlers)
                                        .filter(eventHandler -> eventHandler.eventType.equals(event.event))
                                        .forEach(eventHandler -> eventHandler.consumer.accept(jsonObject));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class EventHandler {

        final String eventType;
        final Consumer<JSONObject> consumer;

        public EventHandler(String eventType, Consumer<JSONObject> consumer) {
            this.eventType = eventType;
            this.consumer = consumer;
        }
    }
}
