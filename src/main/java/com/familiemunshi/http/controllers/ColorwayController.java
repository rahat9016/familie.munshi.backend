package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.AddColorwayImagesRequest;
import com.familiemunshi.http.dtos.requests.CreateColorwayRequest;
import com.familiemunshi.http.dtos.requests.ReorderColorwayImagesRequest;
import com.familiemunshi.http.dtos.requests.UpdateColorwayRequest;
import com.familiemunshi.http.dtos.responses.ColorwayResponse;
import com.familiemunshi.http.dtos.responses.PaginatedResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.service.ColorwayService;
import com.familiemunshi.service.entities.Colorway;
import com.familiemunshi.service.filters.FindAllColorwaysFilter;
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

@Tag(name = "Colorway Management", description = "APIs for managing colorways")
@RestController
@RequestMapping(API_BASE + COLORWAYS)
@RequiredArgsConstructor
public class ColorwayController {

    private final ColorwayService colorwayService;

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'COLORWAY_CREATE')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a colorway. Only marketing name and name required; rest defaulted.", responses = {
            @ApiResponse(responseCode = "201", description = "Colorway created."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions.")
    })
    public ResponseEntity<SuccessResponse<ColorwayResponse>> create(
            @Valid @RequestBody CreateColorwayRequest request) {

        Colorway created = colorwayService.createColorway(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Colorway created successfully", toResponse(created)));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'COLORWAY_LIST_VIEW')")
    @GetMapping
    @Operation(summary = "List colorways.", responses = {
            @ApiResponse(responseCode = "200", description = "Colorways retrieved."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "COLORWAY_LIST_VIEW permission required.")
    })
    public ResponseEntity<PaginatedResponse<ColorwayResponse>> list(
            @Valid @ParameterObject FindAllColorwaysFilter filter,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Records per page, maximum 100", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Colorway> result = colorwayService.getAllColorwaysByFilter(filter, pageable);
        List<ColorwayResponse> body = result.getContent().stream().map(this::toResponse).toList();

        return ResponseEntity.ok(PaginatedResponse.of("Colorways retrieved successfully", result, body));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'COLORWAY_VIEW')")
    @GetMapping(ID)
    @Operation(summary = "Get colorway by ID.", responses = {
            @ApiResponse(responseCode = "200", description = "Colorway retrieved."),
            @ApiResponse(responseCode = "404", description = "Colorway not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<ColorwayResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(SuccessResponse.of(
                "Colorway retrieved successfully", toResponse(colorwayService.getColorwayById(id))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'COLORWAY_UPDATE')")
    @PatchMapping(value = ID, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update colorway fields. Null fields are kept.", responses = {
            @ApiResponse(responseCode = "200", description = "Colorway updated."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "404", description = "Colorway not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<ColorwayResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateColorwayRequest request) {

        return ResponseEntity.ok(SuccessResponse.of(
                "Colorway updated successfully", toResponse(colorwayService.updateColorway(id, request))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'COLORWAY_DELETE')")
    @DeleteMapping(ID)
    @Operation(summary = "Delete a colorway and its images.", responses = {
            @ApiResponse(responseCode = "200", description = "Colorway deleted."),
            @ApiResponse(responseCode = "404", description = "Colorway not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable UUID id) {
        colorwayService.deleteColorway(id);
        return ResponseEntity.ok(SuccessResponse.of("Colorway deleted successfully", null));
    }

    // ---------------------------------------------------------------- Images

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'COLORWAY_UPDATE')")
    @PostMapping(value = COLORWAY_IMAGES, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Append images to a colorway (max 10 total).", responses = {
            @ApiResponse(responseCode = "200", description = "Images added."),
            @ApiResponse(responseCode = "400", description = "Invalid image or limit exceeded."),
            @ApiResponse(responseCode = "404", description = "Colorway not found.")
    })
    public ResponseEntity<SuccessResponse<ColorwayResponse>> addImages(
            @PathVariable UUID id,
            @Valid @ModelAttribute AddColorwayImagesRequest request) {

        return ResponseEntity.ok(SuccessResponse.of(
                "Images added successfully", toResponse(colorwayService.addImages(id, request.getImages()))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'COLORWAY_UPDATE')")
    @DeleteMapping(COLORWAY_IMAGE_ID)
    @Operation(summary = "Remove one image from a colorway.", responses = {
            @ApiResponse(responseCode = "200", description = "Image deleted."),
            @ApiResponse(responseCode = "404", description = "Colorway or image not found.")
    })
    public ResponseEntity<SuccessResponse<ColorwayResponse>> deleteImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId) {

        return ResponseEntity.ok(SuccessResponse.of(
                "Image deleted successfully", toResponse(colorwayService.deleteImage(id, imageId))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'COLORWAY_UPDATE')")
    @PutMapping(value = COLORWAY_IMAGES_ORDER, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reorder colorway images. First image becomes cover.", responses = {
            @ApiResponse(responseCode = "200", description = "Images reordered."),
            @ApiResponse(responseCode = "400", description = "Order must list every image exactly once."),
            @ApiResponse(responseCode = "404", description = "Colorway not found.")
    })
    public ResponseEntity<SuccessResponse<ColorwayResponse>> reorderImages(
            @PathVariable UUID id,
            @Valid @RequestBody ReorderColorwayImagesRequest request) {

        return ResponseEntity.ok(SuccessResponse.of(
                "Images reordered successfully",
                toResponse(colorwayService.reorderImages(id, request.getImageIds()))));
    }

    private ColorwayResponse toResponse(Colorway colorway) {
        return new ColorwayResponse(colorway, colorwayService.resolveImages(colorway));
    }
}
