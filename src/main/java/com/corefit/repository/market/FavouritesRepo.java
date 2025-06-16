package com.corefit.repository.market;

import com.corefit.entity.market.Favourites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface FavouritesRepo extends JpaRepository<Favourites, Long> {

    Optional<Favourites> findByUser_Id(long userId);

    @Query("SELECT p.id FROM Favourites f JOIN f.products p WHERE f.user.id = :userId")
    Set<Long> findFavouriteProductIdsByUserId(@Param("userId") Long userId);
}
