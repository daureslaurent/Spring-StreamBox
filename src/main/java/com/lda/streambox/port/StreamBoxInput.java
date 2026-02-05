package com.lda.streambox.port;

import com.lda.streambox.entity.StreamBoxBaseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StreamBoxInput<T extends StreamBoxBaseEntity> {
    List<T> lockNextBatch(int limit);

    void addEvent(T streamBoxEvent);
    void finish(T streamBoxEvent);

    @Transactional
    void handleEvent(T streamBoxEvent);
}
