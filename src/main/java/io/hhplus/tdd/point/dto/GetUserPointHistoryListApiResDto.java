package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.domain.TransactionType;

import java.io.Serializable;

public record GetUserPointHistoryListApiResDto(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) implements Serializable {
    public static GetUserPointHistoryListApiResDto from(PointHistory pointHistory) {
        return new GetUserPointHistoryListApiResDto(pointHistory.id(), pointHistory.userId(), pointHistory.amount(), pointHistory.type(), pointHistory.updateMillis());
    }
}
