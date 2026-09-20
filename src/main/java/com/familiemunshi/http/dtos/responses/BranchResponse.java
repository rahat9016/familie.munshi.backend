package com.familiemunshi.http.dtos.responses;
import com.familiemunshi.service.entities.Branch;
import lombok.*;

@Setter
@Getter
public class BranchResponse {
    private Long id;
    private String name;
    private String address;
    private String logoUrl;
    private boolean isActive;

    public BranchResponse(Branch branch) {
        this.id = branch.id();
        this.name = branch.name();
        this.address = branch.address();
        this.logoUrl = branch.logoUrl();
        this.isActive = branch.isActive();
    }
}
