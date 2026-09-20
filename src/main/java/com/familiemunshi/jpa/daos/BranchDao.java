package com.familiemunshi.jpa.daos;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="branches")
public class BranchDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name="logo_url")
    private String logoUrl;

    @Builder.Default
    @Column(name="is_active", nullable = false)
    private boolean isActive = true;
}
