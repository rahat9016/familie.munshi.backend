package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.CreateCategoryRequest;
import com.familiemunshi.http.dtos.requests.UpdateCategoryRequest;
import com.familiemunshi.http.dtos.responses.CategoryResponse;
import com.familiemunshi.http.dtos.responses.PaginatedResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.service.CategoryService;
import com.familiemunshi.service.entities.Category;
import com.familiemunshi.service.filters.FindAllCategoriesFilter;
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
import java.util.Map;
import java.util.UUID;

import static com.familiemunshi.common.utils.Constants.*;

@Tag(name = "Category Management", description = "APIs for managing categories (children of departments)")
@RestController
@RequestMapping(API_BASE + CATEGORIES)
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'CATEGORY_CREATE')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a category under a department. isActive defaults to true.", responses = {
            @ApiResponse(responseCode = "201", description = "Category created."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions."),
            @ApiResponse(responseCode = "404", description = "Department not found."),
            @ApiResponse(responseCode = "409", description = "Category already exists in this department.")
    })
    public ResponseEntity<SuccessResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request) {

        Category created = categoryService.createCategory(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Category created successfully", toResponse(created)));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'CATEGORY_LIST_VIEW')")
    @GetMapping
    @Operation(summary = "List categories. Optionally filter by departmentId.", responses = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "CATEGORY_LIST_VIEW permission required.")
    })
    public ResponseEntity<PaginatedResponse<CategoryResponse>> list(
            @Valid @ParameterObject FindAllCategoriesFilter filter,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Records per page, maximum 100", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Category> result = categoryService.getAllCategoriesByFilter(filter, pageable);

        Map<UUID, Long> counts = categoryService.countStyles(
                result.getContent().stream().map(Category::id).toList());
        List<CategoryResponse> body = result.getContent().stream()
                .map(c -> new CategoryResponse(c, counts.getOrDefault(c.id(), 0L)))
                .toList();

        return ResponseEntity.ok(PaginatedResponse.of("Categories retrieved successfully", result, body));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'CATEGORY_VIEW')")
    @GetMapping(ID)
    @Operation(summary = "Get category by ID.", responses = {
            @ApiResponse(responseCode = "200", description = "Category retrieved."),
            @ApiResponse(responseCode = "404", description = "Category not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<CategoryResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(SuccessResponse.of(
                "Category retrieved successfully", toResponse(categoryService.getCategoryById(id))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'CATEGORY_UPDATE')")
    @PatchMapping(value = ID, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update category fields. Null fields are kept.", responses = {
            @ApiResponse(responseCode = "200", description = "Category updated."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "404", description = "Category or department not found."),
            @ApiResponse(responseCode = "409", description = "Category already exists in this department."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<CategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        return ResponseEntity.ok(SuccessResponse.of(
                "Category updated successfully", toResponse(categoryService.updateCategory(id, request))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'CATEGORY_DELETE')")
    @DeleteMapping(ID)
    @Operation(summary = "Delete a category.", responses = {
            @ApiResponse(responseCode = "200", description = "Category deleted."),
            @ApiResponse(responseCode = "404", description = "Category not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(SuccessResponse.of("Category deleted successfully", null));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category, categoryService.countStyles(category.id()));
    }
}
