package com.lda.streambox.factory;

import com.lda.streambox.entity.StreamBoxBasePayloadEntity;
import com.lda.streambox.json.JsonConverter;
import com.lda.streambox.model.StreamBoxEvent;
import com.lda.streambox.register.StreamBoxEventRegistry;

public abstract class InboxFactoryAbstract<T extends StreamBoxBasePayloadEntity, E extends StreamBoxEvent>
        implements InboxFactoryInterface<T, E> {

    protected final StreamBoxEventRegistry registry;
    protected final Class<T> entityClass;


    protected InboxFactoryAbstract(StreamBoxEventRegistry registry, Class<T> entityClass) {
        this.registry = registry;
        this.entityClass = entityClass;
    }

    public E createEvent(T entity, JsonConverter jsonConverter) {
        final var type = entity.getType();
        final var eventClass = registry.resolve(type);
        if (eventClass == null) {
            throw new RuntimeException("Unknown event type: " + type);
        }

        return (E) jsonConverter.fromJson(entity.getPayload(), eventClass);
    }

    @Override
    public T createEntity(String json, JsonConverter jsonConverter) {
        return jsonConverter.fromJson(json, entityClass);
    }
}
