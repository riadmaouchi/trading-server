package org.trading.discovery;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;

public final class Service {

    public final String name;
    public final int port;
    public final long ttl;
    public final String id;
    public final int monitoringPort;
    public final List<String> tags;

    public Service(String name,
                   int port,
                   long ttl,
                   String id,
                   int monitoringPort,
                   String... tags) {
        this.name = name;
        this.port = port;
        this.ttl = ttl;
        this.id = id;
        this.monitoringPort = monitoringPort;
        this.tags = List.of(tags);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Service that = (Service) other;
        return port == that.port &&
                ttl == that.ttl &&
                monitoringPort == that.monitoringPort &&
                Objects.equals(name, that.name) &&
                Objects.equals(id, that.id) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, port, ttl, id, monitoringPort, tags);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("port", port)
                .add("ttl", ttl)
                .add("id", id)
                .add("monitoringPort", monitoringPort)
                .add("tags", tags)
                .toString();
    }
}
