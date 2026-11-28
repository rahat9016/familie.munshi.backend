package com.familiemunshi.service;

import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.dtos.requests.UpdateCategoryRequest;
import com.familiemunshi.jpa.daos.CategoryDao;
import com.familiemunshi.jpa.repositories.CategoryRepository;
import com.familiemunshi.jpa.repositories.DepartmentRepository;
import com.familiemunshi.mapper.CategoryMapper;
import com.familiemunshi.service.entities.Category;
import com.familiemunshi.service.exceptions.CategoryAlreadyExistsException;
import com.familiemunshi.service.exceptions.CategoryNotFoundException;
import com.familiemunshi.service.exceptions.DepartmentNotFoundException;
import com.familiemunshi.service.filters.FindAllCategoriesFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;

    // ------------------------------------------------------------------ CRUD

    public Category createCategory(Category category) {
        ensureDepartmentExists(category.departmentId());
        if (categoryRepository.existsByDepartmentIdAndNameIgnoreCase(category.departmentId(), category.name())) {
            throw new CategoryAlreadyExistsException(category.name());
        }
        CategoryDao saved = categoryRepository.saveAndFlush(CategoryMapper.toDao(category));
        return CategoryMapper.toEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<Category> getAllCategoriesByFilter(FindAllCategoriesFilter filter, Pageable pageable) {
        return categoryRepository.findAllByFilters(
                        Util.toSearchPattern(filter.getSearch()),
                        filter.getDepartmentId(),
                        filter.getIsActive(),
                        pageable)
                .map(CategoryMapper::toEntity);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(UUID id) {
        return CategoryMapper.toEntity(findCategoryDao(id));
    }

    /** One query for all given departments, grouped by departmentId (avoids N+1). */
    @Transactional(readOnly = true)
    public Map<UUID, List<Category>> getCategoriesByDepartmentIds(Collection<UUID> departmentIds) {
        if (departmentIds.isEmpty()) return Map.of();
        return categoryRepository.findAllByDepartmentIdInOrderByNameAsc(departmentIds).stream()
                .map(CategoryMapper::toEntity)
                .collect(Collectors.groupingBy(Category::departmentId));
    }

    public Category updateCategory(UUID id, UpdateCategoryRequest r) {
        CategoryDao dao = findCategoryDao(id);

        UUID targetDepartmentId = r.getDepartmentId() != null ? r.getDepartmentId() : dao.getDepartmentId();
        boolean departmentChanged = !targetDepartmentId.equals(dao.getDepartmentId());
        if (departmentChanged) ensureDepartmentExists(targetDepartmentId);

        String targetName = (r.getName() != null && !r.getName().isBlank())
                ? r.getName().trim()
                : dao.getName();

        if ((departmentChanged || !targetName.equalsIgnoreCase(dao.getName()))
                && categoryRepository.existsByDepartmentIdAndNameIgnoreCaseAndIdNot(targetDepartmentId, targetName, id)) {
            throw new CategoryAlreadyExistsException(targetName);
        }

        dao.setDepartmentId(targetDepartmentId);
        dao.setName(targetName);
        if (r.getIsActive() != null) dao.setActive(r.getIsActive());

        return CategoryMapper.toEntity(categoryRepository.saveAndFlush(dao));
    }

    public void deleteCategory(UUID id) {
        categoryRepository.delete(findCategoryDao(id));
    }

    // -------------------------------------------------------- numberOfStyles

    /** TODO: replace with styleRepository.countByCategoryId(categoryId) once the Style module exists. */
    @Transactional(readOnly = true)
    public long countStyles(UUID categoryId) {
        return 0L;
    }

    /** Batch count for list endpoints (avoids N+1). TODO: single GROUP BY query on styles. */
    @Transactional(readOnly = true)
    public Map<UUID, Long> countStyles(Collection<UUID> categoryIds) {
        return categoryIds.stream().distinct().collect(Collectors.toMap(Function.identity(), id -> 0L));
    }

    // --------------------------------------------------------------- Helpers

    private CategoryDao findCategoryDao(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private void ensureDepartmentExists(UUID departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new DepartmentNotFoundException(departmentId);
        }
    }
}
