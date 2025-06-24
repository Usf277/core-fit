package com.corefit.entity.auth;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "fcm_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class FcmToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
