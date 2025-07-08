package com.corefit.repository.helper;

import com.corefit.entity.helper.AppContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppContentRepo extends JpaRepository<AppContent, Long> {
}
