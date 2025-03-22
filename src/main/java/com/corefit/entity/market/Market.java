package com.corefit.entity.market;

import com.corefit.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "markets")
public class Market {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Category category;

    private String lat;

    private String lng;

    private String address;

    @ManyToOne
    @JsonIgnore
    private User user;

    private String imageUrl;

    @Builder.Default
    private boolean isOpened = true;

    @OneToMany(mappedBy = "market", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<SubCategory> subCategories = new HashSet<>();

    @OneToMany(mappedBy = "market", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "market", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Rate> rates = new HashSet<>();

}
