package com.corefit.test;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PassRepository extends JpaRepository<Pass, Integer> {
    boolean existsByPass(int pass);
}
