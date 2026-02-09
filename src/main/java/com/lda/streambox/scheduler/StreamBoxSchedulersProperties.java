package com.lda.streambox.scheduler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "streambox.scheduler")
@Validated
public class StreamBoxSchedulersProperties {

    // Getters/setters
    /** Global on/off switch for all auto-registered schedulers. */
    private boolean enabled = false;

    /** Global defaults applied to all scheduler instances unless overridden. */
    private ScheduleConfig defaults = ScheduleConfig.defaults();

    /**
     * Per "type" configs (e.g., inbox, outbox). Keys are logical types you choose.
     * Each type inherits from defaults, but can override values.
     */
    private Map<String, ScheduleConfig> types = new HashMap<>();

    /**
     * Per-instance overrides keyed by instance id (e.g., bean name or logical name).
     * Each instance inherits from its type (if any) and then defaults.
     */
    private Map<String, ScheduleConfig> instances = new HashMap<>();

    /** Small value object representing schedule parameters. */
    @Data
    @Validated
    public static class ScheduleConfig {
        // Getters/setters
        /**
         * Fixed rate in ISO-8601 (e.g., "PT7S") or milliseconds as string (e.g., "7000").
         */
        private String fixedRate = "PT7S";

        /**
         * Initial delay (ISO duration or millis string).
         */
        private String initialDelay = "PT0S";

        /**
         * Batch limit for lockNextBatch.
         */
        private int limit = 100;

        public static ScheduleConfig defaults() { return new ScheduleConfig(); }

    }
}