package com.corefit.repository;

import com.corefit.entity.Playground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaygroundRepo extends JpaRepository<Playground, Integer> {
}
