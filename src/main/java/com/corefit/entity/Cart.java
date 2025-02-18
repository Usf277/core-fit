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
@Table(name = "cart", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id"})})
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(nullable = false, unique = true)
    private User user;

    @ManyToOne
    private Market market;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();
}
