package io.hhplus.tdd.domain;

import lombok.Builder;

@Builder
public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}
