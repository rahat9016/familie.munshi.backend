package com.familiemunshi.jpa.daos;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="roles")
public class RoleDao {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String name;

    @Column(columnDefinition= "TEXT")
    private String description;

    @Builder.Default
    @Column(name ="is_system", nullable=false)
    private Boolean isSystem = false;


    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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
                    columnNames = {"role_id", "permissiTon_id"}
            )
    )
    private Set<PermissionsDao> permissions = new HashSet<>();
}
