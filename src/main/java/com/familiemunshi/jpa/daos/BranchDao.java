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

    private String address;

    @Column(nullable = false)
    private boolean isActive;
}
