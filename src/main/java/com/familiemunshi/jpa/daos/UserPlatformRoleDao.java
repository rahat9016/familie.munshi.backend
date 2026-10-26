package com.familiemunshi.jpa.daos;

import com.familiemunshi.common.utils.UuidV7Generator;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "user_platform_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_platform_role",
                        columnNames = {"user_id", "role_id"}
                )
        }, indexes = {
                @Index(
                        name = "idx_user_platform_roles_role",
                        columnList = "role_id"
                )
        })
public class UserPlatformRoleDao {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private UserDao user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="role_id", nullable = false)
    private RoleDao role;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            updatable = true
    )
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UuidV7Generator.generate();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
