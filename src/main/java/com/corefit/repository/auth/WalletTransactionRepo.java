package com.corefit.repository.auth;

import com.corefit.entity.auth.User;
import com.corefit.entity.auth.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepo extends JpaRepository<WalletTransaction, Long> {
    Page<WalletTransaction> findAllByUserOrderByTimestampDesc(User user, Pageable pageable);
}