package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.domain.UserPoint;

import java.io.Serializable;

public record GetUserPointApiResDto(
        long id,
        long point,
        long updateMillis
) implements Serializable {
    public static GetUserPointApiResDto from(UserPoint userPoint){
        return new GetUserPointApiResDto(userPoint.id(), userPoint.point(), userPoint.updateMillis());
    }
}
