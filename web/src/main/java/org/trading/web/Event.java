package org.trading.web;

import com.google.common.base.MoreObjects;

public class Event {
    public final String event;
    public final String data;
    public final String id;
    public final long reconnectTime;

    private Event(String event, String data, String id, long reconnectTime) {
        this.event = event;
        this.data = data;
        this.id = id;
        this.reconnectTime = reconnectTime;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("event", event)
                .add("data", data)
                .add("id", id)
                .add("reconnectTime", reconnectTime)
                .toString();
    }

    public static class Builder {
        private String event;
        private String data;
        private String id;
        private long reconnectTime;

        public static Builder newBuilder() {
            return new Builder();
        }

        public Event build() {
            return new Event(
                    event,
                    data,
                    id,
                    reconnectTime
            );
        }

        public Builder withEvent(final String event) {
            this.event = event;
            return this;
        }

        public Builder withData(final String data) {
            this.data = data;
            return this;
        }

        public Builder withId(final String id) {
            this.id = id;
            return this;
        }

        public Builder withReconnectTime(final long reconnectTime) {
            this.reconnectTime = reconnectTime;
            return this;
        }
    }
}
