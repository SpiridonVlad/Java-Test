package com.example.carins.service;

import com.example.carins.exception.ResourceNotFoundException;
import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.InsurancePolicyCreateDto;
import com.example.carins.web.dto.InsurancePolicyUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsurancePolicyServiceTest {

    @Mock
    private InsurancePolicyRepository policyRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private InsurancePolicyService policyService;

    private Car testCar;
    private InsurancePolicy testPolicy;
    private InsurancePolicyCreateDto createDto;
    private InsurancePolicyUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        Owner owner = new Owner();
        owner.setId(1L);

        testCar = new Car();
        testCar.setId(1L);
        testCar.setMake("Toyota");
        testCar.setModel("Camry");
        testCar.setOwner(owner);

        testPolicy = new InsurancePolicy();
        testPolicy.setId(1L);
        testPolicy.setCar(testCar);
        testPolicy.setProvider("State Farm");
        testPolicy.setStartDate(LocalDate.now());
        testPolicy.setEndDate(LocalDate.now().plusYears(1));

        createDto = new InsurancePolicyCreateDto(
                1L,
                "Allstate",
                LocalDate.now(),
                LocalDate.now().plusYears(1)
        );

        updateDto = new InsurancePolicyUpdateDto(
                1L,
                "Updated Provider",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusYears(1).plusDays(1)
        );
    }

    @Test
    void createPolicy_WithValidData_ShouldReturnSavedPolicy() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(policyRepository.save(any(InsurancePolicy.class))).thenReturn(testPolicy);

        InsurancePolicy result = policyService.createPolicy(createDto);

        assertNotNull(result);
        assertEquals("State Farm", result.getProvider());
        verify(carRepository).findById(1L);
        verify(policyRepository).save(any(InsurancePolicy.class));
    }

    @Test
    void createPolicy_WithInvalidCarId_ShouldThrowResourceNotFoundException() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.createPolicy(createDto)
        );

        assertEquals("Car not found with id: 1", exception.getMessage());
        verify(carRepository).findById(1L);
        verify(policyRepository, never()).save(any());
    }

    @Test
    void getPolicy_WithValidId_ShouldReturnPolicy() {
        when(policyRepository.findByIdWithCarAndOwner(1L)).thenReturn(Optional.of(testPolicy));

        InsurancePolicy result = policyService.getPolicy(1L);

        assertNotNull(result);
        assertEquals(testPolicy.getId(), result.getId());
        verify(policyRepository).findByIdWithCarAndOwner(1L);
    }

    @Test
    void getPolicy_WithInvalidId_ShouldThrowResourceNotFoundException() {
        when(policyRepository.findByIdWithCarAndOwner(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.getPolicy(1L)
        );

        assertEquals("Insurance policy not found with id: 1", exception.getMessage());
        verify(policyRepository).findByIdWithCarAndOwner(1L);
    }

    @Test
    void getAllPolicies_ShouldReturnAllPolicies() {
        List<InsurancePolicy> policies = List.of(testPolicy);
        when(policyRepository.findAllWithCarAndOwner()).thenReturn(policies);

        List<InsurancePolicy> result = policyService.getAllPolicies();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPolicy.getId(), result.get(0).getId());
        verify(policyRepository).findAllWithCarAndOwner();
    }

    @Test
    void getPoliciesByCarId_ShouldReturnCarPolicies() {
        List<InsurancePolicy> policies = List.of(testPolicy);
        when(policyRepository.findByCarIdWithCarAndOwner(1L)).thenReturn(policies);

        List<InsurancePolicy> result = policyService.getPoliciesByCarId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPolicy.getCar().getId(), result.get(0).getCar().getId());
        verify(policyRepository).findByCarIdWithCarAndOwner(1L);
    }

    @Test
    void updatePolicy_WithValidData_ShouldReturnUpdatedPolicy() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(policyRepository.save(any(InsurancePolicy.class))).thenReturn(testPolicy);

        InsurancePolicy result = policyService.updatePolicy(1L, updateDto);

        assertNotNull(result);
        verify(policyRepository).findById(1L);
        verify(carRepository).findById(1L);
        verify(policyRepository).save(testPolicy);
    }

    @Test
    void updatePolicy_WithInvalidId_ShouldThrowResourceNotFoundException() {
        when(policyRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.updatePolicy(1L, updateDto)
        );

        assertEquals("Insurance policy not found with id: 1", exception.getMessage());
        verify(policyRepository).findById(1L);
        verify(policyRepository, never()).save(any());
    }

    @Test
    void updatePolicy_WithInvalidCarId_ShouldThrowResourceNotFoundException() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.updatePolicy(1L, updateDto)
        );

        assertEquals("Car not found with id: 1", exception.getMessage());
        verify(policyRepository).findById(1L);
        verify(carRepository).findById(1L);
        verify(policyRepository, never()).save(any());
    }

    @Test
    void deletePolicy_WithValidId_ShouldDeletePolicy() {
        when(policyRepository.existsById(1L)).thenReturn(true);

        policyService.deletePolicy(1L);

        verify(policyRepository).existsById(1L);
        verify(policyRepository).deleteById(1L);
    }

    @Test
    void deletePolicy_WithInvalidId_ShouldThrowResourceNotFoundException() {
        when(policyRepository.existsById(1L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.deletePolicy(1L)
        );

        assertEquals("Insurance policy not found with id: 1", exception.getMessage());
        verify(policyRepository).existsById(1L);
        verify(policyRepository, never()).deleteById(any());
    }
}
