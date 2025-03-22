package com.corefit.entity.market;

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
@Table(name = "subCategories")
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Market market;

    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Product> products = new HashSet<>();
}
