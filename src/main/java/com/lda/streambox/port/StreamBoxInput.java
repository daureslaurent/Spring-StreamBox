package com.lda.streambox.port;

import java.util.List;

public interface StreamBoxInput<T> {

    List<T> lockNextBatch(int limit);

    void finish(T streamBoxEvent);

    void addToBox(T streamBoxEvent);

}
