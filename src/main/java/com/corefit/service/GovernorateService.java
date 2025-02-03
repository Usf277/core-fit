package com.corefit.service;

import com.corefit.entity.Governorate;
import com.corefit.repository.GovernorateRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GovernorateService {

    @Autowired
    private GovernorateRepo governorateRepo;


    public Governorate insert(Governorate governorate) {

        return governorateRepo.save(governorate);
    }

    public List<Governorate> getAll() {
        return governorateRepo.findAll();
    }
}