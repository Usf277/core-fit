package com.corefit.service;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.ProductDto;
import com.corefit.dto.ProductRequest;
import com.corefit.entity.Market;
import com.corefit.entity.Product;
import com.corefit.entity.SubCategory;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.MarketRepo;
import com.corefit.repository.ProductRepo;
import com.corefit.repository.SubCategoryRepo;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepo productRepo;
    private final MarketRepo marketRepo;
    private final SubCategoryRepo subCategoryRepo;
    private final FilesService filesService;

    public ProductService(ProductRepo productRepo, MarketRepo marketRepo, SubCategoryRepo subCategoryRepo, FilesService filesService) {
        this.productRepo = productRepo;
        this.marketRepo = marketRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.filesService = filesService;
    }

    public GeneralResponse<?> findById(long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Product not found"));

        return new GeneralResponse<>("Success", mapToDto(product));
    }

    public Page<ProductDto> getAll(Integer page, Integer size) {
        size = (size == null || size <= 0) ? 5 : size;
        page = (page == null || page < 1) ? 1 : page;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());

        return productRepo.findAll(pageable).map(this::mapToDto);
    }

    @Transactional
    public GeneralResponse<?> insert(ProductRequest productRequest, List<MultipartFile> images) {
        Market market = marketRepo.findById(productRequest.getMarketId())
                .orElseThrow(() -> new GeneralException("Market not found"));

        SubCategory subCategory = subCategoryRepo.findById(productRequest.getSubCategoryId())
                .orElseThrow(() -> new GeneralException("Sub category not found"));

        List<String> imageUrls = uploadImages(images);

        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .offer(productRequest.getOffer())
                .price(productRequest.getPrice())
                .market(market)
                .subCategory(subCategory)
                .images(imageUrls)
                .build();

        productRepo.save(product);

        return new GeneralResponse<>("Product added successfully", mapToDto(product));
    }

    @Transactional
    public GeneralResponse<?> update(ProductRequest productRequest, List<MultipartFile> images) {
        Product product = productRepo.findById(productRequest.getId())
                .orElseThrow(() -> new GeneralException("Product not found"));

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setOffer(productRequest.getOffer());
        product.setSubCategory(subCategoryRepo.findById(productRequest.getSubCategoryId())
                .orElseThrow(() -> new GeneralException("Sub category not found")));

        if (images != null && !images.isEmpty()) {
            deleteImages(product.getImages());
            product.setImages(uploadImages(images));
        }

        productRepo.save(product);

        return new GeneralResponse<>("Product updated successfully", mapToDto(product));
    }

    @Transactional
    public GeneralResponse<?> delete(long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Product not found"));

        deleteImages(product.getImages());
        productRepo.deleteById(id);

        return new GeneralResponse<>("Product deleted successfully");
    }

    @Transactional
    public GeneralResponse<?> changeStatus(long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Product not found"));

        product.setHidden(!product.isHidden());
        productRepo.save(product);

        return new GeneralResponse<>("Product status changed successfully");
    }


    // Helper methods
    private ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .offer(product.getOffer())
                .subCategoryName(product.getSubCategory().getName())
                .marketName(product.getMarket().getName())
                .images(product.getImages())
                .isHidden(product.isHidden())
                .build();
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return images.stream().map(image -> {
            try {
                return filesService.saveImage(image);
            } catch (IOException e) {
                throw new GeneralException("Failed to upload image: " + e.getMessage());
            }
        }).collect(Collectors.toList());
    }

    private void deleteImages(List<String> images) {
        if (images != null && !images.isEmpty()) {
            images.forEach(image -> {
                try {
                    filesService.deleteImage(image);
                } catch (IOException e) {
                    throw new GeneralException("Failed to delete image: " + e.getMessage());
                }
            });
        }
    }
}
