package com.corefit.entity.playground;

import com.corefit.entity.City;
import com.corefit.entity.User;
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

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    private City city;

    private String address;

    private LocalTime morningShiftStart;

    private LocalTime morningShiftEnd;

    private LocalTime nightShiftStart;

    private LocalTime nightShiftEnd;

    private double bookingPrice;

    @Builder.Default
    private boolean hasExtraPrice = false;

    private double extraNightPrice;

    @Builder.Default
    private boolean isOpened = true;

    @ElementCollection
    @CollectionTable(name = "playgroud_images", joinColumns = @JoinColumn(name = "playground_id"))
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