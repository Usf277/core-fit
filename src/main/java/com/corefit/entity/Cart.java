package com.corefit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CartItem> cartItems;

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id + "," +
                "\"user\":{" +
                "\"id\":" + user.getId() + "," +
                "\"username\":\"" + user.getUsername() + "\"" +
                "}," +
                "\"market\":{" +
                "\"id\":" + market.getId() + "," +
                "\"name\":\"" + market.getName() + "\"" +
                "}," +
                "\"cartItems\":" + cartItems +
                "}";
    }

}
