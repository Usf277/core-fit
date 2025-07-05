package com.corefit.entity.helper;

import com.corefit.enums.AppContentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "app_content")
public class AppContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private AppContentType type; // ABOUT, PRIVACY, TERMS

    @Lob
    @Column(nullable = false)
    private String content;

    private LocalDateTime updatedAt;
}


