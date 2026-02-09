package com.lda.streambox.autoconfig;

import com.lda.streambox.port.StreamBoxInput;
import com.lda.streambox.scheduler.StreamBoxScheduler;
import com.lda.streambox.scheduler.StreamBoxSchedulerRegistry;
import com.lda.streambox.scheduler.StreamBoxSchedulersProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@AutoConfiguration
@EnableConfigurationProperties(StreamBoxSchedulersProperties.class)
@ConditionalOnProperty(prefix = "streambox.scheduler", name = "enabled", havingValue = "true")
public class StreamBoxSchedulersAutoConfiguration {

    /**
     * Default task scheduler if the app didn't define one.
     * You can remove this bean if you mandate apps to provide a TaskScheduler.
     */
    @Bean
    public TaskScheduler streamBoxTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("streambox-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public StreamBoxSchedulerRegistry streamBoxSchedulerRegistry() {
        return new StreamBoxSchedulerRegistry();
    }


    /**
     * Registers a scheduler per StreamBoxInput bean found in the context.
     * This bean is a SmartLifecycle that starts/stops scheduled tasks cleanly.
     */
    @Bean
    @ConditionalOnBean(StreamBoxInput.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public SmartLifecycle streamBoxSchedulersLifecycle(
            Map<String, StreamBoxInput> inputs,
            TaskScheduler taskScheduler,
            StreamBoxSchedulerRegistry streamBoxSchedulerRegistry,
            StreamBoxSchedulersProperties props) {

        return new SmartLifecycle() {
            private volatile boolean running = false;
            private final Map<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

            @Override
            public void start() {
                // For each StreamBoxInput bean, create a scheduler instance and plan it
                inputs.forEach((beanName, input) -> {
                    String type = inferTypeFromBeanNameOrInterface(beanName, input); // "inbox" | "outbox" | other
                    StreamBoxScheduler<?> scheduler = new StreamBoxScheduler<>(input);

                    // Merge defaults → type → instance
                    StreamBoxSchedulersProperties.ScheduleConfig merged = mergeConfig(
                            props.getDefaults(),
                            props.getTypes().get(type),
                            props.getInstances().get(beanName)
                    );

                    Runnable task = scheduler.runnable(merged.getLimit());
                    Duration rate = parseDuration(merged.getFixedRate());
                    Duration initial = parseDuration(merged.getInitialDelay());

                    ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
                            task,
                            Instant.now().plus(initial),
                            rate);

                    futures.put(beanName, future);
                    streamBoxSchedulerRegistry.register(beanName, scheduler);
                });
                running = true;
            }

            @Override
            public void stop() {
                futures.values().forEach(f -> f.cancel(false));
                futures.clear();
                running = false;
            }

            @Override
            public boolean isRunning() { return running; }

            @Override
            public int getPhase() { return Integer.MAX_VALUE; } // start late, stop early
        };
    }

    // ---- helpers ----

    private StreamBoxSchedulersProperties.ScheduleConfig mergeConfig(
            StreamBoxSchedulersProperties.ScheduleConfig defaults,
            StreamBoxSchedulersProperties.ScheduleConfig typeCfg,
            StreamBoxSchedulersProperties.ScheduleConfig instanceCfg) {

        // Simple deep merge: instance overrides type overrides defaults.
        StreamBoxSchedulersProperties.ScheduleConfig merged = new StreamBoxSchedulersProperties.ScheduleConfig();
        // defaults
        merged.setFixedRate(defaults.getFixedRate());
        merged.setInitialDelay(defaults.getInitialDelay());
        merged.setLimit(defaults.getLimit());
        // type
        if (typeCfg != null) {
            if (typeCfg.getFixedRate() != null) merged.setFixedRate(typeCfg.getFixedRate());
            if (typeCfg.getInitialDelay() != null) merged.setInitialDelay(typeCfg.getInitialDelay());
            if (typeCfg.getLimit() > 0) merged.setLimit(typeCfg.getLimit());
        }
        // instance
        if (instanceCfg != null) {
            if (instanceCfg.getFixedRate() != null) merged.setFixedRate(instanceCfg.getFixedRate());
            if (instanceCfg.getInitialDelay() != null) merged.setInitialDelay(instanceCfg.getInitialDelay());
            if (instanceCfg.getLimit() > 0) merged.setLimit(instanceCfg.getLimit());
        }
        return merged;
    }

    private Duration parseDuration(String value) {
        // Accept ISO-8601 (PT7S) or millis string (e.g., "7000")
        if (value == null || value.isBlank()) return Duration.ofSeconds(7);
        if (Character.isDigit(value.charAt(0))) {
            return Duration.ofMillis(Long.parseLong(value));
        }
        return Duration.parse(value);
    }

    private String inferTypeFromBeanNameOrInterface(String beanName, StreamBoxInput<?> input) {
        // Heuristic: decide type based on bean name or package/interface markers
        // You can refine this to use a marker interface (e.g., InboxInput/OutboxInput).
        String lower = beanName.toLowerCase();
        if (lower.contains("inbox")) return "inbox";
        if (lower.contains("outbox")) return "outbox";
        return "default";
    }
}