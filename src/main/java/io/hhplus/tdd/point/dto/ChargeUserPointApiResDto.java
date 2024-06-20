package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.domain.UserPoint;

import java.io.Serializable;

public record ChargeUserPointApiResDto(
        long id,
        long point,
        long updateMillis
) implements Serializable {
    public static ChargeUserPointApiResDto from(UserPoint userPoint){
        return new ChargeUserPointApiResDto(userPoint.id(), userPoint.point(), userPoint.updateMillis());
    }
}