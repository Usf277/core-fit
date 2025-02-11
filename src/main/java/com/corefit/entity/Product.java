package com.corefit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String description;

    private double price;

    private int offer;

    private boolean isHidden = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private Set<ProductImages> images;

    @ManyToOne
    private Market market;

    @ManyToOne
    private SubCategory subCategory;
}
