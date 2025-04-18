package com.corefit.service;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.Governorate;
import com.corefit.repository.GovernorateRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GovernorateService {
    @Autowired
    private GovernorateRepo governorateRepo;

    public Governorate findById(long id) {
        return governorateRepo.findById(id);
    }

    public GeneralResponse<?> getAll() {
        Map<String, Object> data = new HashMap<>();
        data.put("governorates", governorateRepo.findAll());
        return new GeneralResponse<>("Success", data);
    }
}