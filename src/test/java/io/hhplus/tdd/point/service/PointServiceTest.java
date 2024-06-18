package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.validation.PointValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

// PointService 테스트케이스
public class PointServiceTest {
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private UserPointRepository userPointRepository;

    private PointService pointService;

    private PointValidationService pointValidationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        pointValidationService = new PointValidationService();
        pointService = new PointService(pointValidationService, pointHistoryRepository, userPointRepository);
    }
}
