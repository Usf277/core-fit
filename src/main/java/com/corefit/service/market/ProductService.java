package com.corefit.service.market;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.market.ProductResponse;
import com.corefit.dto.request.market.ProductRequest;
import com.corefit.entity.*;
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
import com.corefit.service.FilesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private MarketRepo marketRepo;
    @Autowired
    private SubCategoryRepo subCategoryRepo;
    @Autowired
    private FilesService filesService;
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

        return new GeneralResponse<>("Success", product);
    }

    public GeneralResponse<?> getAll(Integer page, Integer size, Long marketId, Long subCategoryId, String name, HttpServletRequest httpRequest) {
        size = (size == null || size <= 0) ? 5 : size;
        page = (page == null || page < 1) ? 1 : page;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<Product> productsPage = productRepo.findAllByFilters(marketId, subCategoryId, name, pageable);

        long userId = -1;
        Set<Long> favouriteProductIds = new HashSet<>();

        try {
            userId = authService.extractUserIdFromRequest(httpRequest);

            if (userId > 0) {
                User user = authService.findUserById(userId);

                if (user != null && user.getType() == UserType.GENERAL) {
                    favouritesRepo.findByUser_Id(userId).ifPresent(favourites ->
                            favouriteProductIds.addAll(
                                    favourites.getProducts().stream()
                                            .map(Product::getId)
                                            .collect(Collectors.toSet())
                            )
                    );
                }
            }
        } catch (Exception e) {
            throw new GeneralException("Error retrieving user favorites");
        }

        Set<Long> finalFavouriteProductIds = favouriteProductIds;

        Page<ProductResponse> productResponses = productsPage.map(product -> {
            ProductResponse productResponse = mapToDto(product);
            productResponse.setFavourite(finalFavouriteProductIds.contains(product.getId()));
            return productResponse;
        });

        Map<String, Object> data = new HashMap<>();
        data.put("products", productResponses.getContent());
        data.put("totalPages", productResponses.getTotalPages());
        data.put("totalElements", productResponses.getTotalElements());
        data.put("pageSize", productResponses.getSize());

        return new GeneralResponse<>("Products retrieved successfully", data);
    }

    @Transactional
    public GeneralResponse<?> insert(ProductRequest productRequest, List<MultipartFile> images, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }

        Market market = marketRepo.findById(productRequest.getMarketId())
                .orElseThrow(() -> new GeneralException("Market not found"));

        SubCategory subCategory = subCategoryRepo.findById(productRequest.getSubCategoryId())
                .orElseThrow(() -> new GeneralException("Sub category not found"));


        List<String> imageUrls = filesService.uploadImages(images);

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
    public GeneralResponse<?> update(ProductRequest productRequest, List<MultipartFile> newImages) {
        Product product = productRepo.findById(productRequest.getId())
                .orElseThrow(() -> new GeneralException("Product not found"));

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setOffer(productRequest.getOffer());
        product.setSubCategory(subCategoryRepo.findById(productRequest.getSubCategoryId())
                .orElseThrow(() -> new GeneralException("Sub category not found")));

        List<String> oldImages = product.getImages() != null ? product.getImages() : new ArrayList<>();
        List<String> updatedImages = new ArrayList<>();

        List<String> imagesToKeep = productRequest.getImagesToKeep();

        if (imagesToKeep == null || imagesToKeep.isEmpty()) {
            filesService.deleteImages(oldImages);
        } else {
            List<String> imagesToDelete = oldImages.stream()
                    .filter(img -> !imagesToKeep.contains(img))
                    .collect(Collectors.toList());

            filesService.deleteImages(imagesToDelete);
            updatedImages.addAll(imagesToKeep);
        }

        if (newImages != null && !newImages.isEmpty()) {
            List<String> uploaded = filesService.uploadImages(newImages);
            updatedImages.addAll(uploaded);
        }

        product.setImages(updatedImages);
        productRepo.save(product);

        return new GeneralResponse<>("Product updated successfully", product);
    }

    @Transactional
    public GeneralResponse<?> delete(long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Product not found"));

        filesService.deleteImages(product.getImages());
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
