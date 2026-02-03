package com.lda.streambox.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StreamBoxEvent<T>(
        UUID id,
        String type,
        T payload
) {
}
