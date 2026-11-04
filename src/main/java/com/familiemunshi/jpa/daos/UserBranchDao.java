package com.familiemunshi.jpa.daos;

import com.familiemunshi.common.utils.UuidV7Generator;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_branches",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_branch",
                columnNames = {"user_id", "branch_id"}
        ),
        indexes = @Index(
                name = "idx_user_branches_branch",
                columnList = "branch_id"
        )
)
public class UserBranchDao {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserDao user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchDao branch;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UuidV7Generator.generate();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
