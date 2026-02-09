package com.lda.streambox.factory;

import com.lda.streambox.entity.StreamBoxBasePayloadEntity;
import com.lda.streambox.json.JsonConverter;

public interface InboxFactoryInterface<T extends StreamBoxBasePayloadEntity, E> {
    E createEvent(T entity, JsonConverter jsonConverter);
    T createEntity(String json, JsonConverter jsonConverter);
}
