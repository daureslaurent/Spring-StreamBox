package com.lda.streambox.port;

import com.lda.streambox.entity.StreamBoxBaseEntity;

import java.util.List;

public interface StreamBoxInput<T extends StreamBoxBaseEntity> {
    List<T> lockNextBatch(int limit);
    void finish(T streamBoxEntity);
    void doHandle(T streamBoxEntity);
}
