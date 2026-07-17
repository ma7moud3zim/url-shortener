package com.azim.url_shortener.url_shortener.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_blocklist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlBlocklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime blockedAt;

    @PrePersist
    protected void onCreate() {
        blockedAt = LocalDateTime.now();
    }
}