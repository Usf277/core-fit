package com.corefit.repository.auth;

import com.corefit.entity.auth.WalletPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletPaymentRepo extends JpaRepository<WalletPayment, Long> {
    Optional<WalletPayment> findBySessionId(String sessionId);
}
