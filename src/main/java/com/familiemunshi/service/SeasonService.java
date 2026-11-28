package com.familiemunshi.service;

import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.dtos.requests.UpdateSeasonRequest;
import com.familiemunshi.jpa.daos.SeasonDao;
import com.familiemunshi.jpa.repositories.SeasonRepository;
import com.familiemunshi.mapper.SeasonMapper;
import com.familiemunshi.service.entities.Season;
import com.familiemunshi.service.exceptions.SeasonAlreadyExistsException;
import com.familiemunshi.service.exceptions.SeasonNotFoundException;
import com.familiemunshi.service.filters.FindAllSeasonsFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SeasonService {

    private final SeasonRepository seasonRepository;

    // ------------------------------------------------------------------ CRUD

    public Season createSeason(Season season) {
        if (seasonRepository.existsBySeasonIgnoreCase(season.season())) {
            throw new SeasonAlreadyExistsException(season.season());
        }
        SeasonDao saved = seasonRepository.saveAndFlush(SeasonMapper.toDao(season));
        return SeasonMapper.toEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<Season> getAllSeasonsByFilter(FindAllSeasonsFilter filter, Pageable pageable) {
        return seasonRepository.findAllByFilters(
                        Util.toSearchPattern(filter.getSearch()),
                        filter.getStatus(),
                        pageable)
                .map(SeasonMapper::toEntity);
    }

    @Transactional(readOnly = true)
    public Season getSeasonById(UUID id) {
        return SeasonMapper.toEntity(findSeasonDao(id));
    }

    public Season updateSeason(UUID id, UpdateSeasonRequest r) {
        SeasonDao dao = findSeasonDao(id);

        if (r.getSeason() != null && !r.getSeason().isBlank()) {
            String name = r.getSeason().trim();
            if (seasonRepository.existsBySeasonIgnoreCaseAndIdNot(name, id)) {
                throw new SeasonAlreadyExistsException(name);
            }
            dao.setSeason(name);
        }
        if (r.getStatus() != null) dao.setStatus(r.getStatus());

        return SeasonMapper.toEntity(seasonRepository.saveAndFlush(dao));
    }

    public void deleteSeason(UUID id) {
        seasonRepository.delete(findSeasonDao(id));
    }

    // -------------------------------------------------------- numberOfStyles

    /** TODO: replace with styleRepository.countBySeasonId(seasonId) once the Style module exists. */
    @Transactional(readOnly = true)
    public long countStyles(UUID seasonId) {
        return 0L;
    }

    /** Batch count for list endpoint (avoids N+1). TODO: single GROUP BY query on styles. */
    @Transactional(readOnly = true)
    public Map<UUID, Long> countStyles(Collection<UUID> seasonIds) {
        return seasonIds.stream().collect(Collectors.toMap(Function.identity(), id -> 0L));
    }

    // --------------------------------------------------------------- Helpers

    private SeasonDao findSeasonDao(UUID id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new SeasonNotFoundException(id));
    }
}
