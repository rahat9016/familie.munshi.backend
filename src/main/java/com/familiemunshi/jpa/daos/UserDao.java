package com.familiemunshi.jpa.daos;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="users")
public class UserDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="full_name",nullable = false, length = 120)
    private String firstName;

    @Column(name="last_name", nullable = false, length = 120)
    private String lastName;

    @Column(nullable=false, length=150, unique=true)
    private String email;

    @Column(nullable=false, length=255)
    private String password;

    @Builder.Default
    @Column(name="is_system", nullable=false)
    private boolean isSystem = false;

    @Builder.Default
    @Column(name="is_active", nullable=false)
    private boolean isActive  = true;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private BranchDao branch;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleDao role;
}
