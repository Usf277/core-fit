package com.corefit.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "fcm_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FcmToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String token;

    @OneToOne(cascade = CascadeType.ALL)
    private User user;
}
