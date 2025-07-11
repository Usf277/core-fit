package com.corefit.repository.auth;

import com.corefit.entity.auth.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepo extends JpaRepository<FcmToken, Long> {

    public FcmToken findByUserId(Long userId);
}
