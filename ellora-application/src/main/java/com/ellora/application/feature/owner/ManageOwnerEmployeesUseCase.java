package com.ellora.application.feature.owner;

import com.ellora.application.feature.owner.dto.CreateEmployeeRequest;
import com.ellora.application.feature.owner.dto.EmployeeResponse;
import com.ellora.application.feature.owner.dto.UpdateEmployeeRequest;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.SalonRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManageOwnerEmployeesUseCase {

    private final SalonRepository salonRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployees(UUID ownerId) {
        Salon salon = getOwnerSalon(ownerId);
        return employeeRepository.findBySalonId(salon.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public EmployeeResponse createEmployee(UUID ownerId, CreateEmployeeRequest request) {
        Salon salon = getOwnerSalon(ownerId);

        Employee employee = Employee.createNew(
                salon.getId(),
                request.fullName().trim(),
                request.phone() != null ? request.phone().trim() : null,
                request.avatarUrl() != null ? request.avatarUrl().trim() : null,
                request.bio() != null ? request.bio().trim() : null
        );

        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID ownerId, Long employeeId, UpdateEmployeeRequest request) {
        Salon salon = getOwnerSalon(ownerId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessRuleException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));

        if (!employee.getSalonId().equals(salon.getId())) {
            throw new BusinessRuleException("FORBIDDEN", "Nhân viên không thuộc salon của bạn");
        }

        employee.setFullName(request.fullName().trim());
        if (request.phone() != null) {
            employee.setPhone(request.phone().trim());
        }
        if (request.avatarUrl() != null) {
            employee.setAvatarUrl(request.avatarUrl().trim());
        }
        if (request.bio() != null) {
            employee.setBio(request.bio().trim());
        }
        if (request.active() != null) {
            employee.setActive(request.active());
        }

        Employee updated = employeeRepository.save(employee);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteEmployee(UUID ownerId, Long employeeId) {
        Salon salon = getOwnerSalon(ownerId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessRuleException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));

        if (!employee.getSalonId().equals(salon.getId())) {
            throw new BusinessRuleException("FORBIDDEN", "Nhân viên không thuộc salon của bạn");
        }

        // Soft delete
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    private Salon getOwnerSalon(UUID ownerId) {
        if (ownerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập");
        }
        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }
        return salons.get(0);
    }

    private EmployeeResponse mapToResponse(Employee e) {
        return new EmployeeResponse(
                e.getId(),
                e.getSalonId(),
                e.getFullName(),
                e.getPhone(),
                e.getAvatarUrl(),
                e.getBio(),
                e.isActive(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
