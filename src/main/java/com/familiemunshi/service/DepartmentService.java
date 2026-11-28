package com.familiemunshi.service;

import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.dtos.requests.UpdateDepartmentRequest;
import com.familiemunshi.jpa.daos.DepartmentDao;
import com.familiemunshi.jpa.repositories.CategoryRepository;
import com.familiemunshi.jpa.repositories.DepartmentRepository;
import com.familiemunshi.mapper.DepartmentMapper;
import com.familiemunshi.service.entities.Department;
import com.familiemunshi.service.exceptions.DepartmentAlreadyExistsException;
import com.familiemunshi.service.exceptions.DepartmentHasCategoriesException;
import com.familiemunshi.service.exceptions.DepartmentNotFoundException;
import com.familiemunshi.service.filters.FindAllDepartmentsFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;

    // ------------------------------------------------------------------ CRUD

    public Department createDepartment(Department department) {
        if (departmentRepository.existsByNameIgnoreCase(department.name())) {
            throw new DepartmentAlreadyExistsException(department.name());
        }
        DepartmentDao saved = departmentRepository.saveAndFlush(DepartmentMapper.toDao(department));
        return DepartmentMapper.toEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<Department> getAllDepartmentsByFilter(FindAllDepartmentsFilter filter, Pageable pageable) {
        return departmentRepository.findAllByFilters(
                        Util.toSearchPattern(filter.getSearch()),
                        filter.getIsActive(),
                        pageable)
                .map(DepartmentMapper::toEntity);
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(UUID id) {
        return DepartmentMapper.toEntity(findDepartmentDao(id));
    }

    public Department updateDepartment(UUID id, UpdateDepartmentRequest r) {
        DepartmentDao dao = findDepartmentDao(id);

        if (r.getName() != null && !r.getName().isBlank()) {
            String name = r.getName().trim();
            if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new DepartmentAlreadyExistsException(name);
            }
            dao.setName(name);
        }
        if (r.getIsActive() != null) dao.setActive(r.getIsActive());

        return DepartmentMapper.toEntity(departmentRepository.saveAndFlush(dao));
    }

    public void deleteDepartment(UUID id) {
        DepartmentDao dao = findDepartmentDao(id);
        if (categoryRepository.existsByDepartmentId(id)) {
            throw new DepartmentHasCategoriesException(id);
        }
        departmentRepository.delete(dao);
    }

    // --------------------------------------------------------------- Helpers

    private DepartmentDao findDepartmentDao(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }
}
