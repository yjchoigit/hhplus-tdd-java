package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.GetUserPointApiResDto;
import io.hhplus.tdd.point.dto.GetUserPointHistoryListApiResDto;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointControllerTest {

    private static final String LOCAL_HOST = "http://localhost:";
    private static final String PATH = "/point";
    private static String URL = null;

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PointService pointService;

    @BeforeEach
    void setUp() {
        URL = LOCAL_HOST + port + PATH + "/";
    }

    @Test
    @DisplayName("유저 포인트 조회")
    void point() {
        // given
        Long id = 1L;

        // 유저 포인트 충전
        pointService.chargeUserPoint(id, 100L);

        // when
        ResponseEntity<GetUserPointApiResDto> response = restTemplate.getForEntity(URL + id, GetUserPointApiResDto.class);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().point(), 100L);
    }

    @Test
    @DisplayName("유저 포인트 내역 조회")
    void history() {
        // given
        Long id = 2L;
        
        // 유저 포인트 충전
        pointService.chargeUserPoint(id, 100L);

        // when
        ResponseEntity<List<GetUserPointHistoryListApiResDto>> response = restTemplate.exchange(URL + "/" + id + "/histories",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().size(), 1);
    }

    @Test
    @DisplayName("동시에 특정 유저 포인트 충전")
    void charge_concurrent() throws InterruptedException, ExecutionException {
        // given
        Long id = 3L;
        Long firstAmount = 5000L;
        Long chargeAmount = 500L;
        int task = 10;

        // 유저 포인트 충전
        pointService.chargeUserPoint(id, firstAmount);

        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        // 유저 포인트 충전 작업
        for (int i = 0; i < task; i++) {
            CompletableFuture<Void> charge = CompletableFuture.runAsync(() -> {
                try {
                    pointService.chargeUserPoint(id, chargeAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futureList.add(charge);
        }

        // 모든 작업 완료까지 대기
        CompletableFuture<Void> future = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        future.get();

        // then
        GetUserPointApiResDto finalUserPoint = pointService.getUserPoint(id);
        assertEquals(finalUserPoint.point(), firstAmount + (chargeAmount * task));
    }

    @Test
    @DisplayName("동시에 특정 유저 포인트 사용")
    void use_concurrent() throws InterruptedException, ExecutionException {
        // given
        Long id = 4L;
        Long firstAmount = 5000L;
        Long useAmount = 100L;
        int task = 10;

        // 유저 포인트 충전
        pointService.chargeUserPoint(id, firstAmount);

        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        // 포인트 사용 작업
        for (int i = 0; i < task; i++) {
            CompletableFuture<Void> use = CompletableFuture.runAsync(() -> {
                try {
                    pointService.useUserPoint(id, useAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futureList.add(use);
        }

        // 모든 작업 완료까지 대기
        CompletableFuture<Void> future = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        future.get();

        // then
        GetUserPointApiResDto finalUserPoint = pointService.getUserPoint(id);
        assertEquals(finalUserPoint.point(), firstAmount - (useAmount * task));
    }

    @Test
    @DisplayName("동시에 특정 유저 포인트 충전/사용")
    void charge_use_concurrent() throws InterruptedException, ExecutionException {
        // given
        Long id = 5L;
        Long firstAmount = 5000L;
        Long chargeAmount = 500L;
        Long useAmount = 100L;
        int task = 10;

        // 유저 포인트 충전
        pointService.chargeUserPoint(id, firstAmount);

        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        // 유저 포인트 충전 작업
        for (int i = 0; i < task; i++) {
            CompletableFuture<Void> charge = CompletableFuture.runAsync(() -> {
                try {
                    pointService.chargeUserPoint(id, chargeAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futureList.add(charge);
        }

        // 포인트 사용 작업
        for (int i = 0; i < task; i++) {
            CompletableFuture<Void> use = CompletableFuture.runAsync(() -> {
                try {
                    pointService.useUserPoint(id, useAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futureList.add(use);
        }

        // 모든 작업 완료까지 대기
        CompletableFuture<Void> future = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        future.get();

        // then
        GetUserPointApiResDto finalUserPoint = pointService.getUserPoint(id);
        assertEquals(finalUserPoint.point(), firstAmount + (chargeAmount * task) - (useAmount * task));
    }
}