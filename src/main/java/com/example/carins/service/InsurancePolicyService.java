package com.example.carins.service;

import com.example.carins.exception.ResourceNotFoundException;
import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.InsurancePolicyCreateDto;
import com.example.carins.web.dto.InsurancePolicyUpdateDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class InsurancePolicyService {

    private final InsurancePolicyRepository policyRepository;
    private final CarRepository carRepository;

    public InsurancePolicy createPolicy(InsurancePolicyCreateDto dto) {
        log.info("Creating insurance policy for car: {}", dto.carId());

        Car car = carRepository.findById(dto.carId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + dto.carId()));

        InsurancePolicy policy = new InsurancePolicy(
                car,
                dto.provider(),
                dto.startDate(),
                dto.endDate()
        );

        InsurancePolicy savedPolicy = policyRepository.save(policy);
        log.info("Successfully created insurance policy with id: {} for car: {}",
                savedPolicy.getId(), dto.carId());

        return savedPolicy;
    }

    @Transactional(readOnly = true)
    public InsurancePolicy getPolicy(Long id) {
        log.info("Retrieving insurance policy with id: {}", id);
        return policyRepository.findByIdWithCarAndOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance policy not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<InsurancePolicy> getAllPolicies() {
        log.info("Retrieving all insurance policies");
        return policyRepository.findAllWithCarAndOwner();
    }

    @Transactional(readOnly = true)
    public List<InsurancePolicy> getPoliciesByCarId(Long carId) {
        log.info("Retrieving insurance policies for car: {}", carId);
        return policyRepository.findByCarIdWithCarAndOwner(carId);
    }

    public InsurancePolicy updatePolicy(Long id, InsurancePolicyUpdateDto dto) {
        log.info("Updating insurance policy with id: {}", id);

        InsurancePolicy existingPolicy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance policy not found with id: " + id));

        if (dto.carId() != null) {
            Car car = carRepository.findById(dto.carId())
                    .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + dto.carId()));
            existingPolicy.setCar(car);
        }

        if (dto.provider() != null) {
            existingPolicy.setProvider(dto.provider());
        }
        if (dto.startDate() != null) {
            existingPolicy.setStartDate(dto.startDate());
        }
        if (dto.endDate() != null) {
            existingPolicy.setEndDate(dto.endDate());
        }

        InsurancePolicy updatedPolicy = policyRepository.save(existingPolicy);
        log.info("Successfully updated insurance policy with id: {}", id);

        return updatedPolicy;
    }

    public void deletePolicy(Long id) {
        log.info("Deleting insurance policy with id: {}", id);

        if (!policyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Insurance policy not found with id: " + id);
        }

        policyRepository.deleteById(id);
        log.info("Successfully deleted insurance policy with id: {}", id);
    }

    public void fixOpenEndedPolicies() {
        log.info("Fixing open-ended policies by setting default end dates");

        List<InsurancePolicy> openEndedPolicies = policyRepository.findOpenEndedPolicies();

        for (InsurancePolicy policy : openEndedPolicies) {
            LocalDate endDate = policy.getStartDate().plusYears(1);
            policy.setEndDate(endDate);
            policyRepository.save(policy);

            log.info("Fixed open-ended policy {} by setting end date to {}",
                    policy.getId(), endDate);
        }

        log.info("Fixed {} open-ended policies", openEndedPolicies.size());
    }
}
