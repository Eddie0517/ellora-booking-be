package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.model.Employee;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.infrastructure.persistence.entity.EmployeeEntity;
import com.ellora.infrastructure.persistence.mapper.SalonMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataEmployeeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeRepositoryAdapter implements EmployeeRepository {

    private final SpringDataEmployeeRepository springDataEmployeeRepository;
    private final SalonMapper salonMapper;

    @Override
    public Optional<Employee> findById(Long id) {
        return springDataEmployeeRepository.findById(id).map(salonMapper::toDomain);
    }

    @Override
    public List<Employee> findBySalonId(Long salonId) {
        return springDataEmployeeRepository.findBySalonIdOrderByIdAsc(salonId)
                .stream()
                .map(salonMapper::toDomain)
                .toList();
    }

    @Override
    public List<Employee> findBySalonIdAndActiveTrue(Long salonId) {
        return springDataEmployeeRepository.findBySalonIdAndActiveTrue(salonId)
                .stream()
                .map(salonMapper::toDomain)
                .toList();
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeEntity entity = salonMapper.toEntity(employee);
        EmployeeEntity saved = springDataEmployeeRepository.save(entity);
        return salonMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        springDataEmployeeRepository.deleteById(id);
    }
}
