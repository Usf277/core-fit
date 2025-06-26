package com.corefit.service.market;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.market.ProductResponse;
import com.corefit.dto.request.market.ProductRequest;
import com.corefit.entity.auth.User;
import com.corefit.entity.market.Favourites;
import com.corefit.entity.market.Market;
import com.corefit.entity.market.Product;
import com.corefit.entity.market.SubCategory;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.market.FavouritesRepo;
import com.corefit.repository.market.MarketRepo;
import com.corefit.repository.market.ProductRepo;
import com.corefit.repository.market.SubCategoryRepo;
import com.corefit.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProductService {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private MarketRepo marketRepo;
    @Autowired
    private SubCategoryRepo subCategoryRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private FavouritesRepo favouritesRepo;

    public GeneralResponse<?> findById(long id, HttpServletRequest httpRequest) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Product not found"));

        long userId = authService.extractUserIdFromRequest(httpRequest);
        User user = authService.findUserById(userId);

        boolean isFavourite = false;
        if (user.getType() == UserType.GENERAL) {
            Optional<Favourites> favouritesOptional = favouritesRepo.findByUser_Id(userId);
            if (favouritesOptional.isPresent()) {
                isFavourite = favouritesOptional.get().getProducts().stream()
                        .anyMatch(favProduct -> favProduct.getId() == id);
            }
        }
        product.setFavourite(isFavourite);

        Long rateCount = marketRepo.getMarketRateCount(product.getMarket().getId());
        Double averageRate = marketRepo.getMarketAverageRate(product.getMarket().getId());
        averageRate = Math.round(averageRate * 10.0) / 10.0;

        Map<String, Object> data = new HashMap<>();
        data.put("Product", product);
        data.put("rateCount", rateCount != null ? rateCount : 0);
        data.put("averageRate", averageRate != null ? averageRate : 0.0);

        return new GeneralResponse<>("Success", data);
    }

    @Transactional
    public GeneralResponse<?> getAll(Integer page, Integer size, Long marketId, Long subCategoryId, String name, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        if (marketId != null) {
            Market market = marketRepo.findById(marketId)
                    .orElseThrow(() -> new GeneralException("Market not found"));

            if (!market.isOpened() && user.getType().equals(UserType.GENERAL))
                return new GeneralResponse<>("Market is closed", Page.empty());
        }

        Pageable pageable = PageRequest.of(Math.max(page != null ? page - 1 : 0, 0), size != null ? size : 10, Sort.by(Sort.Direction.ASC, "id"));

        Page<Product> productsPage = user.getType() == UserType.PROVIDER
                ? productRepo.findAllByFilters(marketId, subCategoryId, name, pageable)
                : productRepo.findAllByFiltersAndMarketIsOpened(marketId, subCategoryId, name, pageable);


        Set<Long> favouriteProductIds;
        try {
            favouriteProductIds = favouritesRepo.findFavouriteProductIdsByUserId(user.getId());
        } catch (Exception e) {
            throw new GeneralException("Error retrieving user favorites: " + e.getMessage());
        }

        Page<ProductResponse> productResponses = productsPage.map(product -> {
            ProductResponse response = mapToDto(product);
            response.setFavourite(favouriteProductIds.contains(product.getId()));
            return response;
        });

        Map<String, Object> data = new HashMap<>();
        data.put("products", productResponses.getContent());
        data.put("totalPages", productResponses.getTotalPages());
        data.put("totalElements", productResponses.getTotalElements());
        data.put("pageSize", productResponses.getSize());

        return new GeneralResponse<>("Products retrieved successfully", data);
    }

    @Transactional
    public GeneralResponse<?> insert(ProductRequest productRequest, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }

        Market market = marketRepo.findById(productRequest.getMarketId())
                .orElseThrow(() -> new GeneralException("Market not found"));

        SubCategory subCategory = subCategoryRepo.findById(productRequest.getSubCategoryId())
                .orElseThrow(() -> new GeneralException("Sub category not found"));

        List<String> imageUrls = productRequest.getImages();
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new GeneralException("Product images are required");
        }

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

        return new GeneralResponse<>("Product added successfully", product);
    }

    @Transactional
    public GeneralResponse<?> update(ProductRequest productRequest) {
        Product product = productRepo.findById(productRequest.getId())
                .orElseThrow(() -> new GeneralException("Product not found"));

        SubCategory subCategory = subCategoryRepo.findById(productRequest.getSubCategoryId())
                .orElseThrow(() -> new GeneralException("Sub category not found"));

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setOffer(productRequest.getOffer());
        product.setSubCategory(subCategory);

        if (productRequest.getImages() == null || productRequest.getImages().isEmpty()) {
            throw new GeneralException("You must provide at least one image.");
        }

        product.setImages(new ArrayList<>(productRequest.getImages()));
        productRepo.save(product);

        return new GeneralResponse<>("Product updated successfully", product);
    }

    @Transactional
    public GeneralResponse<?> delete(long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Product not found"));
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

    /// Helper methods
    private ProductResponse mapToDto(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .offer(product.getOffer())
                .subCategoryName(product.getSubCategory().getName())
                .marketName(product.getMarket().getName())
                .marketId(product.getMarket().getId())
                .images(product.getImages())
                .isHidden(product.isHidden())
                .isFavourite(product.isFavourite())
                .build();
    }
}
