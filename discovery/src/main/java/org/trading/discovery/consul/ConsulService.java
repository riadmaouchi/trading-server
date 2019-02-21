package org.trading.discovery.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.health.ServiceHealth;
import org.slf4j.Logger;
import org.trading.discovery.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import static com.orbitz.consul.model.agent.Registration.RegCheck.ttl;
import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

public class ConsulService {
    private static final Logger LOGGER = getLogger(ConsulService.class);
    private final Timer timer = new Timer();
    private final Map<String, ServiceHealthCache> listeners = new HashMap<>();
    private HealthClient healthClient;
    private AgentClient agentClient;
    private final KVCache cache;
    private final KeyValueClient kvClient;

    public ConsulService() {
        Consul consul = connectConsul();
        agentClient = consul.agentClient();
        healthClient = consul.healthClient();
        kvClient = consul.keyValueClient();
        cache = KVCache.newCache(kvClient, "/");
        cache.start();
    }

    public void register(Service service, String host, String serviceUrl) {

        ImmutableRegistration registration = ImmutableRegistration.builder()
                .name(service.name)
                .port(service.port)
                .id(service.id)
                .address(host)
                .addChecks(ttl(service.ttl))
                // .addTags("version=" + version)
                .addTags(service.tags.toArray(new String[0]))
                .build();
        agentClient.register(registration);
        kvClient.putValue("service/url/" + service.id, service.tags.get(0) + "://" + serviceUrl + ":" + service.port);
        timer.scheduleAtFixedRate(new PingConsul(service), 0, 2000);
    }

    public void store(Consumer<Map<String, Optional<String>>> consumer) {
        cache.addListener(newValues -> consumer.accept(newValues.entrySet().stream()
                .collect(toMap(Entry::getKey, value -> value.getValue().getValueAsString()))));
    }

    private Consul connectConsul() {
        HostAndPort consulAgent = HostAndPort.fromParts(getProperty("consul.url", "localhost"), 8500);
        LOGGER.info("Connecting to Consul Agent : {}", consulAgent);

        try {
            Consul consul = Consul.builder()
                    .withHostAndPort(consulAgent)
                    .build();
            consul.agentClient().ping();
            return consul;
        } catch (ConsulException e) {
            LOGGER.error("Cannot ping discovery agent {}", e.getLocalizedMessage());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return connectConsul();
        }
    }

    public void registerHealthyNodeListener(String service, ConsulCache.Listener<ServiceHealthKey, ServiceHealth> listener) {

        if (listeners.containsKey(service)) {
            listeners.get(service).addListener(listener);
        } else {
            ServiceHealthCache healthCache = ServiceHealthCache.newCache(healthClient, service);
            healthCache.addListener(listener);
            listeners.put(service, healthCache);
            try {
                healthCache.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PingConsul extends TimerTask {

        private Service service;

        PingConsul(Service service) {
            this.service = service;
        }

        @Override
        public void run() {
            try {
                agentClient.pass(service.id);
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }
    }

}
