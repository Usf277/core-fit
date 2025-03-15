package com.corefit.service.market;

import com.corefit.dto.request.market.FavouriteRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.market.ProductResponse;
import com.corefit.entity.market.Favourites;
import com.corefit.entity.market.Product;
import com.corefit.entity.User;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.market.FavouritesRepo;
import com.corefit.repository.market.ProductRepo;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavouritesService {
    @Autowired
    private FavouritesRepo favouritesRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private AuthService authService;

    public GeneralResponse<?> getFavourites(HttpServletRequest httpRequest) {
        try {
            long userId = authService.extractUserIdFromRequest(httpRequest);

            Optional<Favourites> favouritesOptional = favouritesRepo.findByUser_Id(userId);

            if (favouritesOptional.isEmpty()) {
                return new GeneralResponse<>("Your favourites list is empty", new ArrayList<>());
            }

            List<ProductResponse> favouriteProducts = favouritesOptional.get().getProducts().stream()
                    .map(product -> ProductResponse.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .offer(product.getOffer())
                            .subCategoryName(product.getSubCategory().getName())
                            .marketName(product.getMarket().getName())
                            .images(product.getImages())
                            .isHidden(product.isHidden())
                            .isFavourite(true)
                            .build())
                    .collect(Collectors.toList());

            return new GeneralResponse<>("Favourites retrieved successfully", favouriteProducts);

        } catch (Exception e) {
            throw new GeneralException("An error occurred: " + e.getMessage());
        }
    }

    public GeneralResponse<?> toggleFavourite(FavouriteRequest favouriteRequest, HttpServletRequest httpRequest) {
        try {
            long userId = authService.extractUserIdFromRequest(httpRequest);

            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new GeneralException("User not found"));

            Product product = productRepo.findById(favouriteRequest.getProductId())
                    .orElseThrow(() -> new GeneralException("Product not found"));

            Favourites favourites = favouritesRepo.findByUser_Id(userId).orElseGet(() -> {
                Favourites newFavourites = new Favourites();
                newFavourites.setUser(user);
                newFavourites.setProducts(new ArrayList<>());
                return newFavourites;
            });

            List<Product> favouriteProducts = favourites.getProducts();

            if ("add".equalsIgnoreCase(favouriteRequest.getType())) {
                if (!favouriteProducts.contains(product)) {
                    favouriteProducts.add(product);
                }
            } else if ("remove".equalsIgnoreCase(favouriteRequest.getType())) {
                favouriteProducts.remove(product);
            } else {
                return new GeneralResponse<>("Invalid action type. Use 'add' or 'remove'");
            }

            favouritesRepo.save(favourites);

            return new GeneralResponse<>("Favourites updated successfully");

        } catch (Exception e) {
            throw new GeneralException("An error occurred: " + e.getMessage());
        }
    }

}
