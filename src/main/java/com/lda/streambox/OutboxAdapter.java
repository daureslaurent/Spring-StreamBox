package com.lda.streambox;

import com.lda.streambox.entity.StreamBoxBasePayloadEntity;
import com.lda.streambox.factory.OutboxFactoryInterface;
import com.lda.streambox.json.JsonConverter;
import com.lda.streambox.model.StreamBoxEvent;
import com.lda.streambox.model.StreamBoxWrapper;
import com.lda.streambox.repository.StreamBoxRepository;

public abstract class OutboxAdapter<T extends StreamBoxBasePayloadEntity, E extends StreamBoxEvent>
        extends StreamBoxAbsAdapter<T> {

    protected final JsonConverter jsonConverter;
    protected final StreamBoxRepository<T> outBoxRepository;
    protected final OutboxFactoryInterface<T, StreamBoxWrapper<E>> streamBoxFactory;

    protected OutboxAdapter(JsonConverter jsonConverter, StreamBoxRepository<T> outBoxRepository, OutboxFactoryInterface<T, StreamBoxWrapper<E>> streamBoxFactory) {
        super(outBoxRepository);
        this.jsonConverter = jsonConverter;
        this.outBoxRepository = outBoxRepository;
        this.streamBoxFactory = streamBoxFactory;
    }

    public void addEvent(E streamBoxEvent) {
        final var wrapper = StreamBoxWrapper.<E>builder()
                .type(streamBoxEvent.getClass().getSimpleName())
                .payload(streamBoxEvent)
                .build();
        final var streamBoxEntity = streamBoxFactory.createEntity(wrapper, jsonConverter);
        outBoxRepository.save(streamBoxEntity);
    }

    public void handleEvent(T streamBoxEntity) {
        final var json = jsonConverter.toJson(streamBoxEntity);
        this.sendToMessaging(json);
        this.finish(streamBoxEntity);
    }

    protected abstract void sendToMessaging(final String json);

}
