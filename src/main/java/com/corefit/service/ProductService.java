package com.corefit.service;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.ProductRequest;
import com.corefit.entity.Market;
import com.corefit.entity.Product;
import com.corefit.entity.ProductImages;
import com.corefit.entity.SubCategory;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.MarketRepo;
import com.corefit.repository.ProductRepo;
import com.corefit.repository.ProductImagesRepo;
import com.corefit.repository.SubCategoryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ProductService {
    private final ProductRepo productRepo;
    private final ProductImagesRepo productImagesRepo;
    private final MarketRepo marketRepo;
    private final SubCategoryRepo subCategoryRepo;
    private final FilesService filesService;

    public ProductService(ProductRepo productRepo, ProductImagesRepo productImagesRepo, MarketRepo marketRepo, SubCategoryRepo subCategoryRepo, FilesService filesService) {
        this.productRepo = productRepo;
        this.productImagesRepo = productImagesRepo;
        this.marketRepo = marketRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.filesService = filesService;
    }

    @Transactional
    public GeneralResponse<?> insert(ProductRequest productRequest, Set<MultipartFile> images) {
        Market market = marketRepo.findById(productRequest.getMarketId())
                .orElseThrow(() -> new GeneralException("Market not found"));

        SubCategory subCategory = subCategoryRepo.findById(productRequest.getSubCategoryId())
                .orElseThrow(() -> new GeneralException("Sub category not found"));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setOffer(productRequest.getOffer());
        product.setPrice(productRequest.getPrice());
        product.setMarket(market);
        product.setSubCategory(subCategory);

        product = productRepo.save(product);

        Set<ProductImages> productImagesSet = new HashSet<>();

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                try {
                    String imagePath = filesService.saveImage(image);
                    ProductImages productImage = new ProductImages();
                    productImage.setImageUrl(imagePath);
                    productImage.setProduct(product);
                    productImagesSet.add(productImage);
                } catch (IOException e) {
                    throw new GeneralException("Failed to upload image: " + e.getMessage());
                }
            }
            productImagesRepo.saveAll(productImagesSet);
        }

        product.setImages(productImagesSet);
        productRepo.save(product);

        Map<String, Object> data = new HashMap<>();
        data.put("product", product);

        return new GeneralResponse<>("Product added successfully", data);
    }
}
