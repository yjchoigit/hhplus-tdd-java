package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.validation.PointValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointValidationService pointValidationService;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserPointRepository userPointRepository;

    private final ReentrantLock lock = new ReentrantLock();

    // 유저 포인트 조회
    public UserPoint getUserPoint(Long id) {
        // 유저 id valid 체크
        pointValidationService.checkId(id);
        return userPointRepository.selectById(id);
    }

    // 유저 포인트 내역 조회
    public List<PointHistory> getUserPointHistoryList(Long id) {
        // 유저 id valid 체크
        pointValidationService.checkId(id);
        return pointHistoryRepository.selectAllByUserId(id);
    }

    // 유저 포인트 충전
    public UserPoint chargeUserPoint(Long id, Long amount) {
        try {
            lock.lock();
            // 충전하려는 포인트 valid 체크
            pointValidationService.checkAmount(TransactionType.CHARGE, amount);

            // 유저 포인트 충전
            UserPoint userPoint = userPointRepository.insertOrUpdate(id, amount);

            // 유저 포인트 충전 내역 등록
            pointHistoryRepository.insert(id, amount,
                    TransactionType.CHARGE, System.currentTimeMillis());

            return userPoint;
        } finally {
            lock.unlock();
        }
    }

    // 유저 포인트 사용
    public UserPoint useUserPoint(Long id, Long amount) {
        try {
            lock.lock();
            // 사용하려는 포인트 valid 체크
            pointValidationService.checkAmount(TransactionType.USE, amount);

            // 현재 유저 포인트 조회
            UserPoint nowUserPoint = userPointRepository.selectById(id);

            // 현재 유저 포인트 valid 체크
            pointValidationService.checkNowUserPoint(amount, nowUserPoint.point());

            // 유저 포인트 사용
            UserPoint userPoint = userPointRepository.insertOrUpdate(
                    id, nowUserPoint.point() - amount);

            // 유저 포인트 사용 내역 등록
            pointHistoryRepository.insert(id, amount,
                    TransactionType.USE, System.currentTimeMillis());

            return userPoint;
        } finally {
            lock.unlock();
        }
    }
}
