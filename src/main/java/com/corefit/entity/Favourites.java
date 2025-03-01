package com.corefit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "favourites", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id"})})
public class Favourites {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToMany
    @JoinTable(name = "favourites_products", joinColumns = @JoinColumn(name = "favourites_id"))
    private List<Product> products = new ArrayList<>();
}
