package io.hhplus.tdd.point.service;

import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.domain.TransactionType;
import io.hhplus.tdd.domain.UserPoint;
import io.hhplus.tdd.point.dto.ChargeUserPointApiResDto;
import io.hhplus.tdd.point.dto.GetUserPointApiResDto;
import io.hhplus.tdd.point.dto.GetUserPointHistoryListApiResDto;
import io.hhplus.tdd.point.dto.UseUserPointApiResDto;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.validation.PointValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointValidationService pointValidationService;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserPointRepository userPointRepository;

    private final ReentrantLock lock = new ReentrantLock();

    // 유저 포인트 조회
    public GetUserPointApiResDto getUserPoint(Long id) {
        // 유저 id valid 체크
        pointValidationService.checkId(id);
        return GetUserPointApiResDto.from(userPointRepository.selectById(id));
    }

    // 유저 포인트 내역 조회
    public List<GetUserPointHistoryListApiResDto> getUserPointHistoryList(Long id) {
        // 유저 id valid 체크
        pointValidationService.checkId(id);
        List<PointHistory> list = pointHistoryRepository.selectAllByUserId(id);

        return list.stream().map(GetUserPointHistoryListApiResDto::from).toList();
    }

    // 유저 포인트 충전
    public ChargeUserPointApiResDto chargeUserPoint(Long id, Long amount) {
        try {
            lock.lock();
            // 충전하려는 포인트 valid 체크
            pointValidationService.checkAmount(TransactionType.CHARGE, amount);

            // 유저 포인트 충전
            UserPoint userPoint = userPointRepository.insertOrUpdate(id, amount);

            // 유저 포인트 충전 내역 등록
            pointHistoryRepository.insert(id, amount,
                    TransactionType.CHARGE, System.currentTimeMillis());

            return ChargeUserPointApiResDto.from(userPoint);
        } finally {
            lock.unlock();
        }
    }

    // 유저 포인트 사용
    public UseUserPointApiResDto useUserPoint(Long id, Long amount) {
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

            return UseUserPointApiResDto.from(id, nowUserPoint.point() - amount, System.currentTimeMillis());
        } finally {
            lock.unlock();
        }
    }

}
