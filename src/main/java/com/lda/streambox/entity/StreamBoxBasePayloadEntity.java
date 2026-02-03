package com.lda.streambox.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@MappedSuperclass
public abstract class StreamBoxBasePayloadEntity extends StreamBoxBaseEntity {
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String payload;
}

