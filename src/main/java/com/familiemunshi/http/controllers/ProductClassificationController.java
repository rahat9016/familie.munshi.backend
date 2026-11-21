package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.CreateProductClassificationRequest;
import com.familiemunshi.http.dtos.requests.UpdateProductClassificationRequest;
import com.familiemunshi.http.dtos.responses.PaginatedResponse;
import com.familiemunshi.http.dtos.responses.ProductClassificationResponse;
import com.familiemunshi.http.dtos.responses.ProductClassificationTreeResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.mapper.ProductClassificationMapper;
import com.familiemunshi.service.ProductClassificationService;
import com.familiemunshi.service.entities.ProductClassification;
import com.familiemunshi.service.enums.ProductClassificationLevel;
import com.familiemunshi.service.filters.FindAllProductClassificationsFilter;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.familiemunshi.common.utils.Constants.*;

@RestController
@RequestMapping(API_BASE + PRODUCT_CLASSIFICATIONS)
@RequiredArgsConstructor
@Tag(name = "Product Classifications",
        description = "APIs for managing product classifications")
public class ProductClassificationController {

    private final ProductClassificationService productClassificationService;

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_CREATE')"
    )
    @PostMapping
    @Operation(
            summary = "Create a new product classification.",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Classification created."),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid input."),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthenticated."),
                    @ApiResponse(responseCode = "403",
                            description = "Insufficient permissions."),
                    @ApiResponse(responseCode = "409",
                            description = "Classification conflict.")
            }
    )
    public ResponseEntity<SuccessResponse<ProductClassificationResponse>>
    createClassification(
            @Valid @RequestBody CreateProductClassificationRequest request
    ) {

        ProductClassification savedClassification =
                productClassificationService.createProductClassification(
                        request.toEntity()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        SuccessResponse.of(
                                "Product classification created successfully",
                                new ProductClassificationResponse(savedClassification)
                        )
                );
    }

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_VIEW')"
    )
    @GetMapping
    @Operation(
            summary = "Get all product classifications.",
            description = "Search and filter product classifications by level and status with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Classifications retrieved."),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid pagination or filter parameters."),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthenticated."),
                    @ApiResponse(responseCode = "403",
                            description = "PRODUCT_CLASSIFICATION_VIEW permission required.")
            }
    )
    public ResponseEntity<PaginatedResponse<ProductClassificationResponse>>
    getClassifications(
            @Valid @ParameterObject
            FindAllProductClassificationsFilter filter,

            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(defaultValue = "1")
            @Min(1)
            int page,

            @Parameter(description = "Records per page, maximum 100", example = "20")
            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<ProductClassification> classifications =
                productClassificationService.getAllProductClassificationsByFilter(
                        filter,
                        pageable
                );

        List<ProductClassificationResponse> response = classifications.getContent()
                .stream()
                .map(ProductClassificationResponse::new)
                .toList();

        return ResponseEntity.ok(
                PaginatedResponse.of(
                        "Product classifications retrieved successfully",
                        classifications,
                        response
                )
        );
    }

    @GetMapping(PRODUCT_CLASSIFICATION_TREE)
    @Operation(
            summary = "Get product classifications in tree structure.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Classification tree retrieved."),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthenticated.")
            }
    )
    public ResponseEntity<SuccessResponse<List<ProductClassificationTreeResponse>>>
    getClassificationTree() {
        List<ProductClassificationTreeResponse> response =
                ProductClassificationMapper.toTree(
                        productClassificationService.getActiveClassifications()
                );

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Product classification tree retrieved successfully",
                        response
                )
        );
    }

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_VIEW')"
    )
    @GetMapping(PRODUCT_CLASSIFICATION_ROOTS)
    @Operation(
            summary = "Get root product classifications.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Root classifications retrieved."),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthenticated.")
            }
    )
    public ResponseEntity<SuccessResponse<List<ProductClassificationResponse>>>
    getRootClassifications() {

        List<ProductClassificationResponse> response =
                productClassificationService.getRootClassifications()
                        .stream()
                        .map(ProductClassificationResponse::new)
                        .toList();

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Root classifications retrieved successfully",
                        response
                )
        );
    }

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_VIEW')"
    )
    @GetMapping(ID + PRODUCT_CLASSIFICATION_CHILDREN)
    @Operation(
            summary = "Get child classifications.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Child classifications retrieved."),
                    @ApiResponse(responseCode = "404",
                            description = "Parent classification not found."),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthenticated.")
            }
    )
    public ResponseEntity<SuccessResponse<List<ProductClassificationResponse>>>
    getChildren(
            @PathVariable UUID id
    ) {

        List<ProductClassificationResponse> response =
                productClassificationService.getChildClassifications(id)
                        .stream()
                        .map(ProductClassificationResponse::new)
                        .toList();

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Child classifications retrieved successfully",
                        response
                )
        );
    }

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_VIEW')"
    )
    @GetMapping(PRODUCT_CLASSIFICATION_BY_LEVEL + PRODUCT_CLASSIFICATION_LEVEL)
    @Operation(
            summary = "Get product classifications by level.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Classifications retrieved."),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid classification level."),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthenticated.")
            }
    )
    public ResponseEntity<SuccessResponse<List<ProductClassificationResponse>>>
    getByLevel(
            @PathVariable ProductClassificationLevel level
    ) {

        List<ProductClassificationResponse> response =
                productClassificationService.getClassificationsByLevel(level)
                        .stream()
                        .map(ProductClassificationResponse::new)
                        .toList();

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Product classifications retrieved successfully",
                        response
                )
        );
    }

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_VIEW')"
    )
    @GetMapping(ID)
    @Operation(
            summary = "Get product classification by ID.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Classification retrieved."),
                    @ApiResponse(responseCode = "404",
                            description = "Classification not found."),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthenticated.")
            }
    )
    public ResponseEntity<SuccessResponse<ProductClassificationResponse>>
    getClassification(
            @PathVariable UUID id
    ) {

        ProductClassificationResponse response =
                new ProductClassificationResponse(
                        productClassificationService.getProductClassificationById(id)
                );

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Product classification retrieved successfully",
                        response
                )
        );
    }

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_UPDATE')"
    )
    @PatchMapping(ID)
    @Operation(
            summary = "Update a product classification.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Classification updated."),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid input."),
                    @ApiResponse(responseCode = "404",
                            description = "Classification not found."),
                    @ApiResponse(responseCode = "409",
                            description = "Classification conflict."),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthenticated.")
            }
    )
    public ResponseEntity<SuccessResponse<ProductClassificationResponse>>
    updateClassification(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductClassificationRequest request
    ) {
        ProductClassification updatedClassification =
                productClassificationService.updateProductClassification(
                        id,
                        request.toEntity()
                );

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Product classification updated successfully",
                        new ProductClassificationResponse(updatedClassification)
                )
        );
    }

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_DELETE')"
    )
    @DeleteMapping(ID)
    @Operation(
            summary = "Delete a product classification.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Classification deleted."),
                    @ApiResponse(responseCode = "404",
                            description = "Classification not found."),
                    @ApiResponse(responseCode = "409",
                            description = "Classification has active children."),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthenticated.")
            }
    )
    public ResponseEntity<SuccessResponse<Void>>
    deleteClassification(
            @PathVariable UUID id
    ) {

        productClassificationService.deleteProductClassification(id);

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Product classification deleted successfully",
                        null
                )
        );
    }
}

