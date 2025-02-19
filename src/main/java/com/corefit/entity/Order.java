package com.corefit.entity;

import com.corefit.enums.OrderStatus;
import com.corefit.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(nullable = false)
    private User user;

    private String clientName;

    private String clientAddress;

    private String clientPhone;

    private Double latitude;

    private Double longitude;

    private String additionalInfo;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.ORDER_RECEIVED;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Double totalPrice;

    @ManyToOne
    private Market market;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

}
