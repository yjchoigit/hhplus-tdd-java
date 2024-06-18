package io.hhplus.tdd.point.service.validation;

import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.service.PointEnums;
import org.springframework.stereotype.Service;

@Service
public class PointValidationService {

    // 유저 id는 null이나 0보다 같거나 작을 수 없음
    public void checkId(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException(PointEnums.Error.ID.getMsg());
        }
    }

    // 포인트는 null이나 0일 수 없음
    public void checkAmount(TransactionType transactionType, Long amount) {
        // transactionType에 따른 amount valid 추가 가능성...
        // 현재는 기본만 null or 0
        if (amount == null || amount == 0) {
            throw new RuntimeException(PointEnums.Error.AMOUNT.getMsg());
        }
    }

    // 현재 가지고 있는 포인트는 사용 포인트보다 같거나 커야 함
    public void checkNowUserPoint(Long amount, Long nowPoint) {
            if (nowPoint < amount) {
                throw new RuntimeException(PointEnums.Error.NOT_ENOUGH.getMsg());
        }
    }
}
