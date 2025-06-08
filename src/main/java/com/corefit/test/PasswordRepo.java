package com.corefit.test;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRepo extends JpaRepository<Password, Integer> {
    boolean existsByPass(int pass);
}
