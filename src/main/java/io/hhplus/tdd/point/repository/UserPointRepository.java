package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.domain.UserPoint;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPointRepository {
    UserPoint selectById(Long id);
    UserPoint insertOrUpdate(long id, long amount);
}
