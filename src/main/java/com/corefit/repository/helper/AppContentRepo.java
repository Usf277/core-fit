package com.corefit.repository.helper;

import com.corefit.entity.helper.AppContent;
import com.corefit.enums.AppContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppContentRepo extends JpaRepository<AppContent, Long> {
}
