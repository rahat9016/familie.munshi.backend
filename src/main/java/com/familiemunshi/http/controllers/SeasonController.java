package com.familiemunshi.http.controllers;

import com.familiemunshi.http.dtos.requests.CreateSeasonRequest;
import com.familiemunshi.http.dtos.requests.UpdateSeasonRequest;
import com.familiemunshi.http.dtos.responses.PaginatedResponse;
import com.familiemunshi.http.dtos.responses.SeasonResponse;
import com.familiemunshi.http.dtos.responses.SuccessResponse;
import com.familiemunshi.service.SeasonService;
import com.familiemunshi.service.entities.Season;
import com.familiemunshi.service.filters.FindAllSeasonsFilter;
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

@Tag(name = "Season Management", description = "APIs for managing seasons")
@RestController
@RequestMapping(API_BASE + SEASONS)
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'SEASON_CREATE')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a season. Status defaults to 'On Planning'.", responses = {
            @ApiResponse(responseCode = "201", description = "Season created."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions."),
            @ApiResponse(responseCode = "409", description = "Season already exists.")
    })
    public ResponseEntity<SuccessResponse<SeasonResponse>> create(
            @Valid @RequestBody CreateSeasonRequest request) {

        Season created = seasonService.createSeason(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Season created successfully", toResponse(created)));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'SEASON_LIST_VIEW')")
    @GetMapping
    @Operation(summary = "List seasons.", responses = {
            @ApiResponse(responseCode = "200", description = "Seasons retrieved."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated."),
            @ApiResponse(responseCode = "403", description = "SEASON_LIST_VIEW permission required.")
    })
    public ResponseEntity<PaginatedResponse<SeasonResponse>> list(
            @Valid @ParameterObject FindAllSeasonsFilter filter,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Records per page, maximum 100", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Season> result = seasonService.getAllSeasonsByFilter(filter, pageable);

        Map<UUID, Long> counts = seasonService.countStyles(
                result.getContent().stream().map(Season::id).toList());
        List<SeasonResponse> body = result.getContent().stream()
                .map(s -> new SeasonResponse(s, counts.getOrDefault(s.id(), 0L)))
                .toList();

        return ResponseEntity.ok(PaginatedResponse.of("Seasons retrieved successfully", result, body));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'SEASON_VIEW')")
    @GetMapping(ID)
    @Operation(summary = "Get season by ID.", responses = {
            @ApiResponse(responseCode = "200", description = "Season retrieved."),
            @ApiResponse(responseCode = "404", description = "Season not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<SeasonResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(SuccessResponse.of(
                "Season retrieved successfully", toResponse(seasonService.getSeasonById(id))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'SEASON_UPDATE')")
    @PatchMapping(value = ID, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update season fields. Null fields are kept.", responses = {
            @ApiResponse(responseCode = "200", description = "Season updated."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "404", description = "Season not found."),
            @ApiResponse(responseCode = "409", description = "Season already exists."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<SeasonResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSeasonRequest request) {

        return ResponseEntity.ok(SuccessResponse.of(
                "Season updated successfully", toResponse(seasonService.updateSeason(id, request))));
    }

    @PreAuthorize("@permissionAuthorizationService.hasPermission(authentication, 'SEASON_DELETE')")
    @DeleteMapping(ID)
    @Operation(summary = "Delete a season.", responses = {
            @ApiResponse(responseCode = "200", description = "Season deleted."),
            @ApiResponse(responseCode = "404", description = "Season not found."),
            @ApiResponse(responseCode = "401", description = "Unauthenticated.")
    })
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable UUID id) {
        seasonService.deleteSeason(id);
        return ResponseEntity.ok(SuccessResponse.of("Season deleted successfully", null));
    }

    private SeasonResponse toResponse(Season season) {
        return new SeasonResponse(season, seasonService.countStyles(season.id()));
    }
}
