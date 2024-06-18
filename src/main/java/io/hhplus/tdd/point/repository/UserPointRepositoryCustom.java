package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.UserPoint;

public interface UserPointRepositoryCustom {
    UserPoint selectById(Long id);
    UserPoint insertOrUpdate(long id, long amount);
}
