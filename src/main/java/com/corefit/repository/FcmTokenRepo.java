package com.corefit.repository;

import com.corefit.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepo extends JpaRepository<FcmToken, Long> {

    public FcmToken findByUserId(Long userId);
}
