package com.corefit.service.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.helper.AppContent;
import com.corefit.enums.AppContentType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.helper.AppContentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppContentService {
    @Autowired
    private AppContentRepo appContentRepo;

    public GeneralResponse<?> getContentByType(AppContentType type) {
        AppContent content = appContentRepo.findByType(type)
                .orElseThrow(() -> new GeneralException("Content not found"));
        return new GeneralResponse<>("Content fetched successfully", content.getContent());
    }
}
