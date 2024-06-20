package io.hhplus.tdd.point.service;

import io.hhplus.tdd.domain.PointEnums;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// PointService 테스트케이스
@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private UserPointRepository userPointRepository;
    // 포인트 서비스
    private PointService pointService;
    // 포인트 유효성 검사 서비스
    private PointValidationService pointValidationService;

    // 테스트케이스 네이밍 룰 : Should_기대행위_when_테스트상태

    @BeforeEach
    void setUp() {
        pointValidationService = new PointValidationService();
        pointService = new PointService(pointValidationService, pointHistoryRepository, userPointRepository);
    }

    @Test
    @DisplayName("유저 id가 null 이면 유저 포인트 조회 실패")
    void Should_ThrowExceptionToCheckId_When_IdIsNull() {
        // given
        Long id = null;

        // when, then
        assertThrows(IllegalArgumentException.class, () -> pointValidationService.checkId(id));
    }

    @Test
    @DisplayName("유저 id가 0보다 작으면 유저 포인트 조회 실패")
    void Should_ThrowExceptionToCheckId_When_IdIsLessThanZero() {
        // given
        Long id = -1L;

        // when, then
        assertThrows(IllegalArgumentException.class, () -> pointValidationService.checkId(id));
    }

    @Test
    @DisplayName("유저가 충전하려는 포인트가 null 이면 포인트 충전 실패")
    void Should_ThrowExceptionToCheckAmount_When_AmountIsNull() {
        // given
        Long amount = null;

        // when, then
        assertThrows(IllegalArgumentException.class, () -> pointValidationService.checkAmount(TransactionType.CHARGE, amount));
    }

    @Test
    @DisplayName("유저가 충전하려는 포인트가 0이면 포인트 충전 실패")
    void Should_ThrowExceptionToCheckAmount_When_AmountIsZero() {
        // given
        Long amount = 0L;

        // when, then
        assertThrows(IllegalArgumentException.class, () -> pointValidationService.checkAmount(TransactionType.CHARGE, amount));
    }

    @Test
    @DisplayName("유저가 충전하려는 포인트가 0보다 크면 포인트 충전 성공")
    void Should_DoesNotThrowExceptionToCheckAmount_When_AmountIsGreaterThanZero() {
        // given
        Long amount = 500L;

        // when, then
        assertDoesNotThrow(() -> pointValidationService.checkAmount(TransactionType.CHARGE, amount));
    }
    
    @Test
    @DisplayName("유저가 포인트가 있으면 포인트 조회 성공")
    void Should_SuccessToGetUserPoint_When_UserPointExists() {
        // given
        Long id = 1L;
        UserPoint userPoint = UserPoint.builder()
                .id(id)
                .point(500)
                .updateMillis(System.currentTimeMillis())
                .build();

        // when
        when(userPointRepository.selectById(id)).thenReturn(userPoint);
        GetUserPointApiResDto testResult = pointService.getUserPoint(id);

        // then
        assertEquals(userPoint.point(), testResult.point());
    }

    @Test
    @DisplayName("유저 포인트 내역 조회 성공")
    void Should_SuccessToGetUserPointHistoryList_When_UserPointExists() {
        // given
        Long id = 1L;
        List<PointHistory> pointHistoryList = new ArrayList<>();
        for(int i=1; i<=10; i++) {
            pointHistoryList.add(
                    PointHistory.builder()
                            .id(i)
                            .userId(id)
                            .amount(i % 2 == 0 ? 100 : 100 * i)
                            .type(i % 2 == 0 ? TransactionType.USE : TransactionType.CHARGE)
                            .updateMillis(System.currentTimeMillis())
                            .build()
            );
        }

        // when
        when(pointHistoryRepository.selectAllByUserId(id)).thenReturn(pointHistoryList);
        List<GetUserPointHistoryListApiResDto> testResult = pointService.getUserPointHistoryList(id);

        // then
        assertEquals(testResult.size(), 10);
    }

    @Test
    @DisplayName("포인트 유효성 검사 성공 및 유저 포인트 충전 성공")
    void Should_SuccessToChargeUserPoint_When_CheckAmountSuccess() throws ExecutionException, InterruptedException {
        // given
        Long id = 1L;
        Long initialPoint = 700L;
        Long amount = 100L;

        UserPoint nowUserPoint = UserPoint.builder()
                .id(id)
                .point(initialPoint)
                .updateMillis(System.currentTimeMillis())
                .build();

        UserPoint updateUserPoint = UserPoint.builder()
                .id(id)
                .point(initialPoint + amount)
                .updateMillis(System.currentTimeMillis())
                .build();

        // when
        when(userPointRepository.selectById(id)).thenReturn(nowUserPoint);
        when(userPointRepository.insertOrUpdate(id, nowUserPoint.point() + amount)).thenReturn(updateUserPoint);

        List<CompletableFuture<ChargeUserPointApiResDto>> futures = new ArrayList<>();

        CompletableFuture<ChargeUserPointApiResDto> future = CompletableFuture.supplyAsync(() -> {
            try {
                return pointService.chargeUserPoint(id, amount);
            } catch (Exception e) {
                throw new IllegalArgumentException(PointEnums.Error.NOT_ENOUGH.getMsg());
            }
        });
        futures.add(future);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        allOf.get(); // 모든 작업이 완료될 때까지 대기

        // then
        ChargeUserPointApiResDto result = future.get();
        assertEquals(result.point(), initialPoint + amount);
    }

    @Test
    @DisplayName("잔여 포인트 유효성 검사 성공 시 유저 포인트 사용 성공")
    void Should_TrueToChargeUserPoint_When_CheckAmountTrue() throws InterruptedException, ExecutionException {
        // given
        Long id = 1L;
        Long initialPoint = 700L;
        Long amount = 100L;

        UserPoint nowUserPoint = UserPoint.builder()
                .id(id)
                .point(initialPoint)
                .updateMillis(System.currentTimeMillis())
                .build();

        UserPoint updateUserPoint = UserPoint.builder()
                .id(id)
                .point(initialPoint - amount)
                .updateMillis(System.currentTimeMillis())
                .build();

        // when
        when(userPointRepository.selectById(id)).thenReturn(nowUserPoint);
        when(userPointRepository.insertOrUpdate(id, nowUserPoint.point() - amount)).thenReturn(updateUserPoint);

        List<CompletableFuture<UseUserPointApiResDto>> futures = new ArrayList<>();

        CompletableFuture<UseUserPointApiResDto> future = CompletableFuture.supplyAsync(() -> {
            try {
                return pointService.useUserPoint(id, amount);
            } catch (Exception e) {
                throw new IllegalArgumentException(PointEnums.Error.NOT_ENOUGH.getMsg());
            }
        });
        futures.add(future);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        allOf.get(); // 모든 작업이 완료될 때까지 대기

        // then
        UseUserPointApiResDto result = future.get();
        assertEquals(result.point(), initialPoint - amount);
    }

    @Test
    @DisplayName("잔여 포인트 유효성 검사 실패 시 유저 포인트 사용 실패")
    void Should_FailToUseUserPoint_When_CheckNowUserPointFail() {
        // given
        Long id = 1L;
        Long amount = 700L;
        UserPoint nowUserPoint = UserPoint.builder()
                .id(id)
                .point(600L)
                .updateMillis(System.currentTimeMillis())
                .build();

        // when
        lenient().when(userPointRepository.selectById(id)).thenReturn(nowUserPoint);

        // then
        assertThrows(IllegalArgumentException.class, () -> pointValidationService.checkNowUserPoint(amount, nowUserPoint.point()));
    }

}
