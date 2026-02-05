package com.lda.streambox.port;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StreamBoxInput<T> {

    List<T> lockNextBatch(int limit);

    void finish(T streamBoxEvent);

    void addToBox(T streamBoxEvent);

    @Transactional
    void handleStreamBox(T streamBoxEvent);
}
