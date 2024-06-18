package io.hhplus.tdd.point;

import lombok.Builder;

@Builder
public record UserPoint(
        long id,
        long point,
        long updateMillis
) {
    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }
}
