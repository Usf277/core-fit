package com.corefit.service;

import com.corefit.dto.FavouriteRequest;
import com.corefit.dto.GeneralResponse;
import com.corefit.entity.Favourites;
import com.corefit.entity.Product;
import com.corefit.entity.User;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.FavouritesRepo;
import com.corefit.repository.ProductRepo;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
            long userId = Long.parseLong(authService.extractUserIdFromRequest(httpRequest));
            Map<String, Object> data = new HashMap<>();

            Optional<Favourites> favouritesOptional = favouritesRepo.findByUser_Id(userId);

            if (favouritesOptional.isEmpty()) {
                data.put("products", new ArrayList<>());
                return new GeneralResponse<>("Your favourites list is empty", data);
            }

            List<Product> favouriteProducts = favouritesOptional.get().getProducts();

            data.put("products", favouriteProducts);
            return new GeneralResponse<>("Favourites retrieved successfully", data);

        } catch (Exception e) {
            throw new GeneralException("An error occurred: " + e.getMessage());
        }
    }

    public GeneralResponse<?> toggleFavourite(FavouriteRequest favouriteRequest, HttpServletRequest httpRequest) {
        try {
            long userId = Long.parseLong(authService.extractUserIdFromRequest(httpRequest));

            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new GeneralException("User not found"));

            Product product = productRepo.findById(favouriteRequest.getProductId())
                    .orElseThrow(() -> new GeneralException("Product not found"));


            Favourites favourites = favouritesRepo.findByUser_Id(userId).orElse(new Favourites());

            favourites.setUser(user);

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

            favourites.setProducts(favouriteProducts);
            favouritesRepo.save(favourites);

            return new GeneralResponse<>("Favourites updated successfully", favouriteProducts);

        } catch (Exception e) {
            throw new GeneralException("An error occurred: " + e.getMessage());
        }
    }
}
