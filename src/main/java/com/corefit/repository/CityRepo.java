package com.corefit.repository;

import com.corefit.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepo extends JpaRepository<City, Long> {
    public City findById(long id);

    public List<City> findAllByGovernorateId(long governorateId);
}
