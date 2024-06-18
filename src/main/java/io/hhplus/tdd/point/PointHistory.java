package io.hhplus.tdd.point;

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
