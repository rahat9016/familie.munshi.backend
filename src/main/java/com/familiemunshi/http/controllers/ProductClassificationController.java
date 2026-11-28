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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new product classification.",
            description = "Multipart form. Icon is either a lucide-react name (icon) "
                    + "or an uploaded SVG/PNG/WEBP/JPG file up to 512 KB (iconFile), not both.",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Classification created."),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid input or icon."),
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
            @Valid @ModelAttribute CreateProductClassificationRequest request
    ) {

        ProductClassification savedClassification =
                productClassificationService.createProductClassification(
                        request.toEntity(),
                        request.getIconFile()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        SuccessResponse.of(
                                "Product classification created successfully",
                                toResponse(savedClassification)
                        )
                );
    }

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_VIEW')"
    )
    @GetMapping
    @Operation(
            summary = "Get all product classifications.",
            description = "Search and filter product classifications by level and status with pagination. "
                    + "Ordered by hierarchy level, then position.",
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

        // Hierarchy ordering is defined in the repository query.
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<ProductClassification> classifications =
                productClassificationService.getAllProductClassificationsByFilter(
                        filter,
                        pageable
                );

        List<ProductClassificationResponse> response = classifications.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(
                PaginatedResponse.of(
                        "Product classifications retrieved successfully",
                        classifications,
                        response
                )
        );
    }

    @GetMapping(PRODUCT_CLASSIFICATION_TREE + PRODUCT_CLASSIFICATION_GROUP_ID)
    @Operation(
            summary = "Get one group's classification tree.",
            description = "Returns the group with all of its active descendants. "
                    + "Other groups are not included.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Group tree retrieved."),
                    @ApiResponse(responseCode = "400",
                            description = "Classification is not a GROUP."),
                    @ApiResponse(responseCode = "404",
                            description = "Group not found or inactive."),
            }
    )
    public ResponseEntity<SuccessResponse<ProductClassificationTreeResponse>>
    getGroupTree(
            @PathVariable UUID groupId
    ) {
        List<ProductClassificationTreeResponse> roots =
                ProductClassificationMapper.toTree(
                        productClassificationService.getActiveClassificationsByGroup(groupId),
                        productClassificationService::getIconUrl
                );

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Product classification tree retrieved successfully",
                        roots.getFirst()
                )
        );
    }

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
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Root classifications retrieved successfully",
                        response
                )
        );
    }

    @GetMapping(ID + PRODUCT_CLASSIFICATION_CHILDREN)
    @Operation(
            summary = "Get child classifications.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Child classifications retrieved."),
                    @ApiResponse(responseCode = "404",
                            description = "Parent classification not found."),
            }
    )
    public ResponseEntity<SuccessResponse<List<ProductClassificationResponse>>>
    getChildren(
            @PathVariable UUID id
    ) {

        List<ProductClassificationResponse> response =
                productClassificationService.getChildClassifications(id)
                        .stream()
                        .map(this::toResponse)
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
                        .map(this::toResponse)
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
            description = "Returns active and inactive classifications.",
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

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Product classification retrieved successfully",
                        toResponse(productClassificationService.getProductClassificationById(id))
                )
        );
    }

    @PreAuthorize(
            "@permissionAuthorizationService.hasPermission(authentication, 'PRODUCT_CLASSIFICATION_UPDATE')"
    )
    @PatchMapping(value = ID, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update a product classification.",
            description = "Multipart form. Send icon (lucide-react name) or iconFile to change the icon, "
                    + "icon=\"\" to remove it, or neither to keep it. isActive toggles status.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Classification updated."),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid input or icon."),
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
            @Valid @ModelAttribute UpdateProductClassificationRequest request
    ) {
        ProductClassification updatedClassification =
                productClassificationService.updateProductClassification(
                        id,
                        request.toEntity(),
                        request.getIconFile(),
                        request.getIsActive()
                );

        return ResponseEntity.ok(
                SuccessResponse.of(
                        "Product classification updated successfully",
                        toResponse(updatedClassification)
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
                            description = "Classification has child classifications."),
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

    private ProductClassificationResponse toResponse(ProductClassification classification) {
        return new ProductClassificationResponse(
                classification,
                productClassificationService.getIconUrl(classification)
        );
    }
}
