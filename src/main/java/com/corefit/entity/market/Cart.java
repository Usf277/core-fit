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
@Table(name = "cart", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id"})})
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(nullable = false, unique = true)
    private User user;

    @ManyToOne
    private Market market;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    private double totalPrice;

    public void addItemToCart(CartItem cartItem) {
        cartItems.add(cartItem);
        updateTotalPrice();
    }

    public void updateTotalPrice() {
        this.totalPrice = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
    }
}
