package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.CreateDepartmentRequest;
import com.familiemunshi.http.dtos.requests.UpdateDepartmentRequest;
import com.familiemunshi.http.dtos.responses.DepartmentResponse;
import com.familiemunshi.http.dtos.responses.PaginatedResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.service.CategoryService;
import com.familiemunshi.service.DepartmentService;
import com.familiemunshi.service.entities.Category;
import com.familiemunshi.service.entities.Department;
import com.familiemunshi.service.filters.FindAllDepartmentsFilter;
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

@Tag(name = "Department Management", description = "APIs for managing departments")
@RestController
@RequestMapping(API_BASE + DEPARTMENTS)
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CategoryService categoryService;

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'DEPARTMENT_CREATE')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a department. isActive defaults to true.", responses = {
            @ApiResponse(responseCode = "201", description = "Department created."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions."),
            @ApiResponse(responseCode = "409", description = "Department already exists.")
    })
    public ResponseEntity<SuccessResponse<DepartmentResponse>> create(
            @Valid @RequestBody CreateDepartmentRequest request) {

        Department created = departmentService.createDepartment(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Department created successfully", toResponse(created)));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'DEPARTMENT_LIST_VIEW')")
    @GetMapping
    @Operation(summary = "List departments, each with its categories.", responses = {
            @ApiResponse(responseCode = "200", description = "Departments retrieved."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "DEPARTMENT_LIST_VIEW permission required.")
    })
    public ResponseEntity<PaginatedResponse<DepartmentResponse>> list(
            @Valid @ParameterObject FindAllDepartmentsFilter filter,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Records per page, maximum 100", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Department> result = departmentService.getAllDepartmentsByFilter(filter, pageable);

        return ResponseEntity.ok(PaginatedResponse.of(
                "Departments retrieved successfully", result, toResponses(result.getContent())));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'DEPARTMENT_VIEW')")
    @GetMapping(ID)
    @Operation(summary = "Get department by ID, with its categories.", responses = {
            @ApiResponse(responseCode = "200", description = "Department retrieved."),
            @ApiResponse(responseCode = "404", description = "Department not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<DepartmentResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(SuccessResponse.of(
                "Department retrieved successfully", toResponse(departmentService.getDepartmentById(id))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'DEPARTMENT_UPDATE')")
    @PatchMapping(value = ID, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update department fields. Null fields are kept.", responses = {
            @ApiResponse(responseCode = "200", description = "Department updated."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "404", description = "Department not found."),
            @ApiResponse(responseCode = "409", description = "Department already exists."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<DepartmentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDepartmentRequest request) {

        return ResponseEntity.ok(SuccessResponse.of(
                "Department updated successfully", toResponse(departmentService.updateDepartment(id, request))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'DEPARTMENT_DELETE')")
    @DeleteMapping(ID)
    @Operation(summary = "Delete a department. Fails if it still has categories.", responses = {
            @ApiResponse(responseCode = "200", description = "Department deleted."),
            @ApiResponse(responseCode = "404", description = "Department not found."),
            @ApiResponse(responseCode = "409", description = "Department still has categories."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(SuccessResponse.of("Department deleted successfully", null));
    }

    // --------------------------------------------------------------- Helpers

    private DepartmentResponse toResponse(Department department) {
        return toResponses(List.of(department)).get(0);
    }

    /** One category query + one style-count batch per call, regardless of page size. */
    private List<DepartmentResponse> toResponses(List<Department> departments) {
        Map<UUID, List<Category>> categoriesByDepartment = categoryService.getCategoriesByDepartmentIds(
                departments.stream().map(Department::id).toList());

        Map<UUID, Long> styleCounts = categoryService.countStyles(
                categoriesByDepartment.values().stream()
                        .flatMap(List::stream)
                        .map(Category::id)
                        .toList());

        return departments.stream().map(d -> {
            List<Category> categories = categoriesByDepartment.getOrDefault(d.id(), List.of());
            long numberOfStyles = categories.stream()
                    .mapToLong(c -> styleCounts.getOrDefault(c.id(), 0L))
                    .sum();
            List<String> categoryNames = categories.stream().map(Category::name).toList();
            return new DepartmentResponse(d, numberOfStyles, categoryNames);
        }).toList();
    }
}
