package com.corefit.repository.helper;


import com.corefit.entity.helper.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepo extends JpaRepository<ContactMessage, Long> {
}
