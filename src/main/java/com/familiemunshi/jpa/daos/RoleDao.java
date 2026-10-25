package com.familiemunshi.jpa.daos;

import com.familiemunshi.common.utils.UuidV7Generator;
import lombok.*;
import com.familiemunshi.service.enums.RoleScope;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="roles",
        indexes = {
                @Index(name = "idx_roles_scope", columnList = "scope"),
                @Index(name = "idx_roles_branch", columnList = "branch_id"),
                @Index(name = "idx_roles_is_active", columnList = "is_active")
        }
)
public class RoleDao {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable=false, length=100)
    private String name;

    @Column(columnDefinition= "TEXT")
    private String description;

    @Builder.Default
    @Column(name ="is_system", nullable=false)
    private Boolean isSystem = false;

    @Enumerated(EnumType.STRING)
    @Column(name="scope", nullable=false, length=20)
    private RoleScope scope;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name= "branch_id")
    private BranchDao branch;

    @Builder.Default
    @ManyToMany(fetch= FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name="role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_role_permission",
                    columnNames = {"role_id", "permission_id"}
            )
    )
    private Set<PermissionDao> permissions = new HashSet<>();


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
