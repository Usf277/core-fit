package com.corefit.service.playground;

import com.corefit.dto.request.playground.PlaygroundFavouriteRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.User;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.PlaygroundFavourite;
import com.corefit.repository.playground.PlaygroundFavouritesRepo;
import com.corefit.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PlaygroundFavouriteService {
    @Autowired
    private PlaygroundFavouritesRepo playgroundFavouritesRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    @Lazy
    private PlaygroundService playgroundService;


    @Transactional
    public GeneralResponse<?> toggleFavourite(PlaygroundFavouriteRequest request, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Long userId = user.getId();
        Long playgroundId = request.getPlaygroundId();

        Playground playground = playgroundService.findById(playgroundId);

        PlaygroundFavourite favourites = playgroundFavouritesRepo.findByUserId(userId)
                .orElseGet(() -> {
                    PlaygroundFavourite newFavourites = new PlaygroundFavourite();
                    newFavourites.setUser(user);
                    newFavourites.setPlaygrounds(new ArrayList<>());
                    return newFavourites;
                });

        boolean isCurrentlyFavorite = favourites.getPlaygrounds().stream()
                .anyMatch(p -> p.getId().equals(playgroundId));

        String actionType = request.getType();
        if (actionType == null) {
            actionType = isCurrentlyFavorite ? "remove" : "add";
        }

        if ("add".equalsIgnoreCase(actionType) && !isCurrentlyFavorite) {
            favourites.getPlaygrounds().add(playground);
            playgroundFavouritesRepo.save(favourites);
            return new GeneralResponse<>("Playground added to favorites successfully", true);
        } else if ("remove".equalsIgnoreCase(actionType) && isCurrentlyFavorite) {
            favourites.getPlaygrounds().removeIf(p -> p.getId().equals(playgroundId));
            playgroundFavouritesRepo.save(favourites);
            return new GeneralResponse<>("Playground removed from favorites successfully", false);
        }

        boolean currentStatus = "add".equalsIgnoreCase(actionType);
        return new GeneralResponse<>("Playground favorite status unchanged", currentStatus);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getFavourites(HttpServletRequest httpRequest) {
        long userId = authService.extractUserIdFromRequest(httpRequest);

        Optional<PlaygroundFavourite> favouritesOptional = playgroundFavouritesRepo.findByUserId(userId);

        if (favouritesOptional.isEmpty()) {
            return new GeneralResponse<>("Your favorites list is empty", Collections.emptyList());
        }

        List<Playground> favoritePlaygrounds = favouritesOptional.get().getPlaygrounds();
        favoritePlaygrounds.forEach(playground -> playground.setFavourite(true));

        return new GeneralResponse<>("Favorites retrieved successfully", favoritePlaygrounds);
    }

    ///  Helper method
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndPlaygroundId(Long userId, Long playgroundId) {
        return playgroundFavouritesRepo.existsByUserIdAndPlaygroundId(userId, playgroundId);
    }

    @Transactional(readOnly = true)
    public Set<Long> getFavouritePlaygroundIdsForUser(Long userId) {
        return new HashSet<>(playgroundFavouritesRepo.findFavouritePlaygroundIdsByUserId(userId));
    }
}