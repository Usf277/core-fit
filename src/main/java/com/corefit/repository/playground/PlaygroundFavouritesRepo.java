package com.corefit.repository.playground;

import com.corefit.entity.playground.PlaygroundFavourite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaygroundFavouritesRepo extends JpaRepository<PlaygroundFavourite, Long> {

    Optional<PlaygroundFavourite> findByUserId(long userId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM PlaygroundFavourite f JOIN f.playgrounds p " +
            "WHERE f.user.id = :userId AND p.id = :playgroundId")
    boolean existsByUserIdAndPlaygroundId(@Param("userId") Long userId, @Param("playgroundId") Long playgroundId);

    @Query("SELECT p.id FROM PlaygroundFavourite f JOIN f.playgrounds p WHERE f.user.id = :userId")
    List<Long> findFavouritePlaygroundIdsByUserId(@Param("userId") Long userId);
}