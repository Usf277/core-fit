package com.corefit.repository;

import com.corefit.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepo extends JpaRepository<City, Long> {
    City findById(long id);

    List<City> findAllByGovernorateId(long governorateId);

    boolean existsByNameAndGovernorateId(String name, Long governorateId);
}
