package com.lda.streambox;

import com.lda.streambox.entity.StreamBoxBasePayloadEntity;
import com.lda.streambox.factory.InboxFactoryAbstract;
import com.lda.streambox.json.JsonConverter;
import com.lda.streambox.model.StreamBoxEvent;
import com.lda.streambox.repository.StreamBoxRepository;

public abstract class InboxAdapter<E extends StreamBoxBasePayloadEntity, T extends StreamBoxEvent>
        extends StreamBoxAbsAdapter<E> {

    protected final JsonConverter jsonConverter;
    protected final StreamBoxRepository<E> inBoxRepository;
    protected final InboxFactoryAbstract<E, T> streamBoxFactory;

    protected InboxAdapter(
            JsonConverter jsonConverter,
            StreamBoxRepository<E> inBoxRepository,
            InboxFactoryAbstract<E, T> streamBoxFactory
    ) {
        super(inBoxRepository);
        this.jsonConverter = jsonConverter;
        this.inBoxRepository = inBoxRepository;
        this.streamBoxFactory = streamBoxFactory;
    }

    public void addFromConsumer(String json) {
        final var entity = streamBoxFactory.createEntity(json, jsonConverter);
        inBoxRepository.save(entity);
    }

    public void handleEvent(E entity) {
        final var event = streamBoxFactory.createEvent(entity, jsonConverter);
        this.handleProjection(event);
        this.finish(entity);
    }

    protected abstract void handleProjection(final T event);

}
