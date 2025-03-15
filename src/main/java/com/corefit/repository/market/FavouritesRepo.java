package com.corefit.repository.market;

import com.corefit.entity.market.Favourites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavouritesRepo extends JpaRepository<Favourites, Long> {


    Optional<Favourites> findByUser_Id(long userId);
}
