package com.corefit.entity.market;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Product product;

    private int quantity;

    private double total;

    public void updateTotal() {
        if (product.getOffer() == 0)
            this.total = quantity * product.getPrice();
        else
            this.total = quantity * (product.getPrice() - (product.getPrice() * product.getOffer() / 100));
    }
}

