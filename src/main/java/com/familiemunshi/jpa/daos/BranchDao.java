package com.familiemunshi.jpa.daos;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="branches",
        indexes = {
            @Index(name = "idx_branch_name", columnList = "name"),
            @Index(name = "idx_branch_code", columnList = "code"),
            @Index(name = "idx_branch_is_active", columnList = "is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_branch_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_branch_code", columnNames = "code")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name="logo_url")
    private String logoUrl;

    @Builder.Default
    @Column(name="is_active", nullable = false)
    private boolean isActive = true;


    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
