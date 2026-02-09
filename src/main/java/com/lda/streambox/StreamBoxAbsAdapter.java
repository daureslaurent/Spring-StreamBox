package com.lda.streambox;

import com.lda.streambox.entity.StreamBoxBasePayloadEntity;
import com.lda.streambox.entity.StreamBoxBaseStatusEnum;
import com.lda.streambox.port.StreamBoxInput;
import com.lda.streambox.repository.StreamBoxRepository;

import java.util.List;

public abstract class StreamBoxAbsAdapter
        <T extends StreamBoxBasePayloadEntity>
        implements StreamBoxInput<T> {

    protected final StreamBoxRepository<T> streamBoxRepository;

    protected StreamBoxAbsAdapter(StreamBoxRepository<T> streamBoxRepository) {
        this.streamBoxRepository = streamBoxRepository;
    }

    @Override
    public List<T> lockNextBatch(int limit) {
        return streamBoxRepository.lockNextBatch(limit);
    }

    @Override
    public void finish(T streamBoxEntity) {
        streamBoxEntity.setStatus(StreamBoxBaseStatusEnum.FINISHED);
        streamBoxRepository.save(streamBoxEntity);
    }

}
