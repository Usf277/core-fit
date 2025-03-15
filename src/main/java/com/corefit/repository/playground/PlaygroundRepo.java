package com.corefit.repository.playground;

import com.corefit.entity.playground.Playground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaygroundRepo extends JpaRepository<Playground, Integer> {
}
