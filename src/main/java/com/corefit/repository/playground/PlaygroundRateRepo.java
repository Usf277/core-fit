package com.corefit.repository.playground;

import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.PlaygroundRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PlaygroundRateRepo extends JpaRepository<PlaygroundRate, Long> {
    Optional<PlaygroundRate> findById(Long id);

    Page<PlaygroundRate> findAllByPlaygroundId(Long playgroundId, Pageable pageable);

    List<PlaygroundRate> findByPlayground(Playground playground);

    long countByPlayground(Playground playground);
}
