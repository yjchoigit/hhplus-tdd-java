package io.hhplus.tdd.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


public class PointEnums {

    @Getter
    @RequiredArgsConstructor
    public enum Error {
        ID("유저 ID를 확인해주세요."),
        AMOUNT("포인트를 확인해주세요."),
        NOT_ENOUGH("잔고가 부족합니다. 잔고를 확인해주세요.")
        ;

        private final String msg;
    }
}
