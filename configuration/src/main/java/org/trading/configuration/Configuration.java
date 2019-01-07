package org.trading.configuration;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;
import org.slf4j.Logger;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public final class Configuration {
    private static final Logger LOGGER = getLogger(Configuration.class);

    private final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder;

    private Configuration(final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder) {
        this.builder = builder;
    }

    public static Configuration create() {
        ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new ReloadingFileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                        .configure(new Parameters()
                                .fileBased()
                                .setURL(getResource("application.properties"))
                                .setEncoding(UTF_8.name()));

        builder.setAutoSave(true);
        PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(
                builder.getReloadingController(),
                null,
                1,
                SECONDS,
                newScheduledThreadPool(4)
        );
        trigger.start();
        return new Configuration(builder);
    }

    public int getInt(String prefix, String key) {
        try {
            return this.builder.getConfiguration().immutableSubset(prefix).getInt(key);
        } catch (ConfigurationException e) {
            LOGGER.warn("FAILED to fetch configuration");
            throw new RuntimeException("FAILED to fetch configuration", e);
        }
    }

    public String getString(String key) {
        try {
            return this.builder.getConfiguration().getString(key);
        } catch (ConfigurationException e) {
            LOGGER.warn("FAILED to fetch configuration");
            throw new RuntimeException("FAILED to fetch configuration", e);
        }
    }

    public int getInt(String key) {
        try {
            return this.builder.getConfiguration().getInt(key);
        } catch (ConfigurationException e) {
            LOGGER.warn("FAILED to fetch configuration");
            throw new RuntimeException("FAILED to fetch configuration", e);
        }
    }
}
