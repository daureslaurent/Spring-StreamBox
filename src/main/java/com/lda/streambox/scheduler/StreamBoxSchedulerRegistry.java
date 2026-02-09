package com.lda.streambox.scheduler;

import com.lda.streambox.entity.StreamBoxBaseEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamBoxSchedulerRegistry {

    private final Map<String, StreamBoxScheduler<?>> schedulers = new ConcurrentHashMap<>();

    public void register(String name, StreamBoxScheduler<?> scheduler) {
        schedulers.put(name, scheduler);
    }

    @SuppressWarnings("unchecked")
    public <T extends StreamBoxScheduler<?>> T get(String name) {
        return (T) schedulers.get(name);
    }

    public Map<String, StreamBoxScheduler<? extends StreamBoxBaseEntity>> all() {
        return Map.copyOf(schedulers);
    }
}