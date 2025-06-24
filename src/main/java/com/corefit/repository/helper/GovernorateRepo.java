package com.corefit.repository.helper;

import com.corefit.entity.helper.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GovernorateRepo extends JpaRepository<Governorate, Long> {

    public Governorate findById(long id);
}
