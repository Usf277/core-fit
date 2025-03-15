package com.corefit.service.market;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.market.SubCategoryRequest;
import com.corefit.entity.market.Market;
import com.corefit.entity.market.SubCategory;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.market.MarketRepo;
import com.corefit.repository.market.SubCategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SubCategoryService {

    @Autowired
    private SubCategoryRepo subCategoryRepo;
    @Autowired
    private MarketRepo marketRepo;


    public GeneralResponse<?> getSubCategoriesByMarketId(long marketId) {
        return new GeneralResponse<>("Success", subCategoryRepo.getSubCategoriesByMarketId(marketId));
    }

    public GeneralResponse<?> insert(SubCategoryRequest subCategoryRequest) {
        Market market = marketRepo.findById(subCategoryRequest.getMarketId())
                .orElseThrow(() -> new GeneralException("Market not found"));

        Map<String, Object> data = new HashMap<>();
        SubCategory subCategory = new SubCategory();

        subCategory.setName(subCategoryRequest.getName());
        subCategory.setMarket(market);

        subCategoryRepo.save(subCategory);
        data.put("SubCategory", subCategory);
        return new GeneralResponse<>("SubCategory added successfully", data);
    }

    public GeneralResponse<?> update(SubCategoryRequest subCategoryRequest) {
        SubCategory subCategory = subCategoryRepo.findById(subCategoryRequest.getId())
                .orElseThrow(() -> new GeneralException("SubCategory not found"));

        Map<String, Object> data = new HashMap<>();
        subCategory.setName(subCategoryRequest.getName());

        subCategoryRepo.save(subCategory);
        data.put("SubCategory", subCategory);
        return new GeneralResponse<>("SubCategory updated successfully", data);
    }

    public GeneralResponse<?> delete(long id) {
        SubCategory subCategory = subCategoryRepo.findById(id)
                .orElseThrow(() -> new GeneralException("SubCategory not found"));

        subCategoryRepo.deleteById(id);
        return new GeneralResponse<>("SubCategory deleted successfully");
    }
}