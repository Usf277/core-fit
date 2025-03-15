package com.corefit.entity.playground;

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

    @Column(nullable = false)
    private String lat;

    @Column(nullable = false)
    private String lng;

    private String address;

    private int teamMembers;

    private LocalTime morningShiftStart;

    private LocalTime morningShiftEnd;

    private LocalTime nightShiftStart;

    private LocalTime nightShiftEnd;

    private double bookingPrice;

    private boolean hasExtraPrice;

    private double extraNightPrice;

    private boolean isOpened;

    @ElementCollection
    @CollectionTable(name = "playgroud_images", joinColumns = @JoinColumn(name = "playground_id"))
    @Column(name = "image_url")
    private List<String> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @JsonIgnore
    private User user;
}