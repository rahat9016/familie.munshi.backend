package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.CreateBrandRequest;
import com.familiemunshi.http.dtos.requests.UpdateBrandRequest;
import com.familiemunshi.http.dtos.responses.BrandResponse;
import com.familiemunshi.http.dtos.responses.PaginatedResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.service.BrandService;
import com.familiemunshi.service.entities.Brand;
import com.familiemunshi.service.filters.FindAllBrandsFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Brand Management", description = "APIs for managing brands")
@RestController
@RequestMapping(API_BASE + BRANDS)
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'BRAND_CREATE')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new brand.", responses = {
            @ApiResponse(responseCode = "201", description = "Brand created."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions."),
            @ApiResponse(responseCode = "409", description = "Title or position conflict.")
    })
    public ResponseEntity<SuccessResponse<BrandResponse>> createBrand(
            @Valid @ModelAttribute CreateBrandRequest request) {

        Brand created = brandService.createBrand(request.toEntity(), request.getLogo());
        BrandResponse response = new BrandResponse(created, brandService.getBrandLogoUrl(created));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Brand created successfully", response));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'BRAND_LIST_VIEW')")
    @GetMapping
    @Operation(summary = "Get all brands.", responses = {
            @ApiResponse(responseCode = "200", description = "Brands retrieved."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "BRAND_LIST_VIEW permission required.")
    })
    public ResponseEntity<PaginatedResponse<BrandResponse>> getBrands(
            @Valid @ParameterObject FindAllBrandsFilter filter,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Records per page, maximum 100", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("position")));
        Page<Brand> brands = brandService.getAllBrandsByFilter(filter, pageable);

        List<BrandResponse> response = brands.getContent().stream()
                .map(brand -> new BrandResponse(brand, brandService.getBrandLogoUrl(brand)))
                .toList();

        return ResponseEntity.ok(PaginatedResponse.of("Brands retrieved successfully", brands, response));
    }

    @GetMapping(PUBLIC)
    @Operation(summary = "Get active brands (public).", responses = {
            @ApiResponse(responseCode = "200", description = "Active brands retrieved.")
    })
    public ResponseEntity<SuccessResponse<List<BrandResponse>>> getPublicBrands() {
        List<BrandResponse> response = brandService.getPublicBrands().stream()
                .map(brand -> new BrandResponse(brand, brandService.getBrandLogoUrl(brand)))
                .toList();

        return ResponseEntity.ok(SuccessResponse.of("Brands retrieved successfully", response));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'BRAND_VIEW')")
    @GetMapping(ID)
    @Operation(summary = "Get brand by ID.", responses = {
            @ApiResponse(responseCode = "200", description = "Brand retrieved."),
            @ApiResponse(responseCode = "404", description = "Brand not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<BrandResponse>> getBrand(@PathVariable UUID id) {
        Brand brand = brandService.getBrandById(id);
        return ResponseEntity.ok(SuccessResponse.of(
                "Brand retrieved successfully",
                new BrandResponse(brand, brandService.getBrandLogoUrl(brand))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'BRAND_UPDATE')")
    @PatchMapping(value = ID, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a brand.", responses = {
            @ApiResponse(responseCode = "200", description = "Brand updated."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "404", description = "Brand not found."),
            @ApiResponse(responseCode = "409", description = "Title or position conflict."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<BrandResponse>> updateBrand(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdateBrandRequest request) {

        Brand updated = brandService.updateBrand(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                "Brand updated successfully",
                new BrandResponse(updated, brandService.getBrandLogoUrl(updated))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'BRAND_DELETE')")
    @DeleteMapping(ID)
    @Operation(summary = "Delete a brand.", responses = {
            @ApiResponse(responseCode = "200", description = "Brand deleted."),
            @ApiResponse(responseCode = "404", description = "Brand not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<Void>> deleteBrand(@PathVariable UUID id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(SuccessResponse.of("Brand deleted successfully", null));
    }
}
