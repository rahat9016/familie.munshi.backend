package com.familiemunshi.jpa.daos;

import com.familiemunshi.service.enums.ProductClassificationLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "product_classifications",
        indexes = {
                @Index(
                        name = "idx_product_classifications_parent",
                        columnList = "parent_id"
                ),
                @Index(
                        name = "idx_product_classifications_level",
                        columnList = "level"
                ),
                @Index(
                        name = "idx_product_classifications_active",
                        columnList = "is_active"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_product_classifications_level_sort_order",
                        columnNames = {"level", "sort_order"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductClassificationDao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductClassificationLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductClassificationDao parent;

    /*
     * Single icon column: S3 key ("public/...") for uploaded files,
     * or a lucide-react icon name.
     */
    @Column(length = 255)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private Integer position;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
