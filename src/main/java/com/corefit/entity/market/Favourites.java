package com.corefit.entity.market;

import com.corefit.entity.User;
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
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToMany
    @JoinTable(name = "favourites_products", joinColumns = @JoinColumn(name = "favourites_id"))
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
