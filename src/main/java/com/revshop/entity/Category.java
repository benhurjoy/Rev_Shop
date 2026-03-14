package com.revshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "categories_seq")
    @SequenceGenerator(name = "categories_seq", sequenceName = "categories_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // ── NEW: variant flags ─────────────────────────────────────
    // Boolean (wrapper) so existing NULL rows don't throw on hydration.
    // isHasColors() / isHasSizes() helper methods below default nulls to false.
    @Column(name = "has_colors")
    @Builder.Default
    private Boolean hasColors = false;

    @Column(name = "has_sizes")
    @Builder.Default
    private Boolean hasSizes = false;

    public boolean isHasColors() { return Boolean.TRUE.equals(hasColors); }
    public boolean isHasSizes()  { return Boolean.TRUE.equals(hasSizes);  }
    // ──────────────────────────────────────────────────────────

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Product> products = new ArrayList<>();
}