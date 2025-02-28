package com.corefit.repository;

import com.corefit.entity.Favourites;
import com.corefit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavouritesRepo extends JpaRepository<Favourites, Long> {


    Optional<Favourites> findByUser_Id(long userId);
}
