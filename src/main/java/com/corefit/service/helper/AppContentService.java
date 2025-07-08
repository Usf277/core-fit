package com.corefit.service.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.helper.AppContent;
import com.corefit.enums.AppContentType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.helper.AppContentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class AppContentService {
    @Autowired
    private AppContentRepo appContentRepo;

    public GeneralResponse<Map<AppContentType, String>> getAllContent() {
        List<AppContent> contents = appContentRepo.findAll();

        Map<AppContentType, String> response = new EnumMap<>(AppContentType.class);
        for (AppContent content : contents) {
            response.put(content.getType(), content.getContent());
        }

        return new GeneralResponse<>("App content fetched successfully", response);
    }
}
