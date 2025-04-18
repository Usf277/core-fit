package com.corefit.repository;

import com.corefit.entity.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GovernorateRepo extends JpaRepository<Governorate, Long> {

    public Governorate findById(long id);
}
