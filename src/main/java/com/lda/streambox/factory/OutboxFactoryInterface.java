package com.lda.streambox.factory;

import com.lda.streambox.json.JsonConverter;

public interface OutboxFactoryInterface<T, E> {
    T createEntity(E event, JsonConverter jsonConverter);
}
