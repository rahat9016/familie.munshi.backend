package com.familiemunshi.http.dtos.responses;
import com.familiemunshi.service.entities.Branch;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
public class BranchResponse {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private String code;
    private String logoUrl;
    private boolean isActive;

    public BranchResponse(Branch branch) {
        this.id = branch.id();
        this.name = branch.name();
        this.address = branch.address();
        this.phone = branch.phone();
        this.code = branch.code();
        this.logoUrl = branch.logoUrl();
        this.isActive = branch.isActive();
    }
}
