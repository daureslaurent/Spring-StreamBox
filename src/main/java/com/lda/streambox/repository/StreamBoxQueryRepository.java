package com.lda.streambox.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;

@NoRepositoryBean
public interface StreamBoxQueryRepository<T> {

    @Query(value = """
        SELECT * FROM #{#entityName}
        WHERE status = 'PENDING'
        ORDER BY created_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<T> lockNextBatch(@Param("limit") int limit);

}
