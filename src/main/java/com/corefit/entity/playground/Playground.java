package com.corefit.entity.playground;

import com.corefit.entity.helper.City;
import com.corefit.entity.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "playgrounds")
public class Playground {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(nullable = false)
    private City city;

    private String address;

    @Column(nullable = false)
    private LocalTime morningShiftStart;

    @Column(nullable = false)
    private LocalTime morningShiftEnd;

    @Column(nullable = false)
    private LocalTime nightShiftStart;

    @Column(nullable = false)
    private LocalTime nightShiftEnd;

    @Column(nullable = false)
    private Double bookingPrice;

    @Builder.Default
    private boolean hasExtraPrice = false;

    private double extraNightPrice;

    @Column(nullable = false)
    private Integer teamMembers;

    @Column(length = 60)
    private String password; // Hashed

    @Builder.Default
    @Column(nullable = false)
    private boolean isOpened = true;

    @ElementCollection
    @CollectionTable(name = "playground_images", joinColumns = @JoinColumn(name = "playground_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    @OrderColumn(name = "image_order")
    private List<String> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "playground", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PlaygroundRate> rates;

    private int avgRate;

    @Transient
    private boolean isFavourite;
}