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
@Table(name="permissions")
public class PermissionDao {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(unique=true, nullable=false, length=255)
    private String name;

    @Column(length=1000)
    private String description;

    private Instant createdAt;

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
