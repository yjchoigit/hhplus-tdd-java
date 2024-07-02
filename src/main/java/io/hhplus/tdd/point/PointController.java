package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.ChargeUserPointApiResDto;
import io.hhplus.tdd.point.dto.GetUserPointApiResDto;
import io.hhplus.tdd.point.dto.GetUserPointHistoryListApiResDto;
import io.hhplus.tdd.point.dto.UseUserPointApiResDto;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/point")
@RestController
@RequiredArgsConstructor
public class PointController {

    private final PointService service;

    // 유저 포인트 조회
    @GetMapping("{id}")
    public GetUserPointApiResDto point(@PathVariable(name = "id") Long id) {
        return service.getUserPoint(id);
    }

    // 유저 포인트 내역 조회
    @GetMapping("{id}/histories")
    public List<GetUserPointHistoryListApiResDto> history(@PathVariable(name = "id") Long id) {
        return service.getUserPointHistoryList(id);
    }

    // 유저 포인트 충전
    @PatchMapping("{id}/charge")
    public ChargeUserPointApiResDto charge(@PathVariable(name = "id") Long id, @RequestBody Long amount) {
        return service.chargeUserPoint(id, amount);
    }

    // 유저 포인트 사용
    @PatchMapping("{id}/use")
    public UseUserPointApiResDto use(@PathVariable(name = "id") Long id, @RequestBody Long amount) {
        return service.useUserPoint(id, amount);
    }
}
