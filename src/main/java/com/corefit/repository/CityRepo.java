package com.corefit.repository;

import com.corefit.entity.City;
import com.corefit.entity.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepo extends JpaRepository<City, Long> {

    public City findById(long id);
}
