package com.lda.streambox;

import com.lda.streambox.entity.StreamBoxBaseEntity;
import com.lda.streambox.entity.StreamBoxBaseStatusEnum;
import com.lda.streambox.port.StreamBoxInput;
import com.lda.streambox.repository.StreamBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public abstract class StreamBoxAbsAdapter<T extends StreamBoxBaseEntity>
        implements StreamBoxInput<T> {

    private final StreamBoxRepository<T> queryRepository;

    @Override
    public List<T> lockNextBatch(int limit) {
        return queryRepository.lockNextBatch(limit);
    }

    @Override
    public void finish(T streamBoxEvent) {
        streamBoxEvent.setStatus(StreamBoxBaseStatusEnum.FINISHED);
        queryRepository.save(streamBoxEvent);
    }

    @Override
    public void addToBox(T productOutboxEventEntity) {
        queryRepository.save(productOutboxEventEntity);
    }


    @Override
    @Transactional
    public void handleStreamBox(T streamBoxEvent) {
        doHandle(streamBoxEvent);
        finish(streamBoxEvent);
    }

    protected abstract void doHandle(T streamBoxEvent);

}
