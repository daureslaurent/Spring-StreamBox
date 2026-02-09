package com.lda.streambox.scheduler;

import com.lda.streambox.entity.StreamBoxBaseEntity;
import com.lda.streambox.port.StreamBoxInput;

public class StreamBoxScheduler<T extends StreamBoxBaseEntity> {

    private final StreamBoxInput<T> streamBoxInput;

    public StreamBoxScheduler(StreamBoxInput<T> streamBoxInput) {
        this.streamBoxInput = streamBoxInput;
    }

    public void consume(int limit) {
        streamBoxInput
                .lockNextBatch(limit)
                .forEach(streamBoxInput::doHandle);
    }

    public Runnable runnable(int limit) {
        return () -> consume(limit);
    }


}
