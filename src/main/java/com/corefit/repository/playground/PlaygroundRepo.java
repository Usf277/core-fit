package com.corefit.repository.playground;

import com.corefit.entity.playground.Playground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaygroundRepo extends JpaRepository<Playground, Integer> {

    Optional<Playground> findById(long id);

    List<Playground> findAllByUser_Id(Long userId);
}
