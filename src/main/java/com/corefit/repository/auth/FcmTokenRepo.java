package com.corefit.repository.auth;

import com.corefit.entity.auth.FcmToken;
import com.corefit.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepo extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByToken(String token);

    void deleteByToken(String token);

    List<FcmToken> findAllByUser(User user);

}
