package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.domain.UserPoint;

import java.io.Serializable;

public record UseUserPointApiResDto(
        long id,
        long point,
        long updateMillis
) implements Serializable {
    public static UseUserPointApiResDto from(long id, long point, long updateMillis){
        return new UseUserPointApiResDto(id, point, updateMillis);
    }
}