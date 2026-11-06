package com.ellora.application.feature.salon;

import com.ellora.application.feature.salon.dto.EmployeeResponse;
import com.ellora.domain.model.Employee;
import com.ellora.domain.repository.EmployeeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSalonStaffUseCase {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> execute(Long salonId) {
        List<Employee> staff = employeeRepository.findBySalonIdAndActiveTrue(salonId);
        return staff.stream()
                .map(e -> new EmployeeResponse(
                        e.getId(),
                        e.getSalonId(),
                        e.getFullName(),
                        e.getPhone(),
                        e.getAvatarUrl(),
                        e.getBio(),
                        e.isActive()
                ))
                .toList();
    }
}
