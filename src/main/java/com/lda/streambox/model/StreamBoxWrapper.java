package com.lda.streambox.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StreamBoxWrapper<T>(
        UUID id,
        String type,
        T payload
) {
}
