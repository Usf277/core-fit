package com.corefit.entity.auth;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "fcm_tokens")
public class FcmToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
