package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.repo.InsurancePolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyExpirationServiceTest {

    @Mock
    private InsurancePolicyRepository policyRepository;
    
    private PolicyExpirationService policyExpirationService;
    
    @BeforeEach
    void setUp() {
        policyExpirationService = new PolicyExpirationService(policyRepository);
    }
    
    @Test
    void checkExpiredPolicies_WithExpiredPolicies_LogsOnce() {
        LocalDate today = LocalDate.now();
        
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        
        InsurancePolicy expiredPolicy = new InsurancePolicy(car, "GEICO", today.minusYears(1), today);
        when(policyRepository.findPoliciesExpiringOnDate(today)).thenReturn(List.of(expiredPolicy));

        policyExpirationService.checkExpiredPolicies();
        policyExpirationService.checkExpiredPolicies();

        verify(policyRepository, times(2)).findPoliciesExpiringOnDate(today);
    }
    
    @Test
    void checkExpiredPolicies_NoExpiredPolicies_NoLogging() {
        LocalDate today = LocalDate.now();
        when(policyRepository.findPoliciesExpiringOnDate(today)).thenReturn(List.of());

        policyExpirationService.checkExpiredPolicies();

        verify(policyRepository).findPoliciesExpiringOnDate(today);
    }
    
    @Test
    void resetLoggedPolicies_ClearsLoggedPolicies() {
        LocalDate today = LocalDate.now();
        
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        
        InsurancePolicy expiredPolicy = new InsurancePolicy(car, "GEICO", today.minusYears(1), today);
        when(policyRepository.findPoliciesExpiringOnDate(today)).thenReturn(List.of(expiredPolicy));

        policyExpirationService.checkExpiredPolicies();
        policyExpirationService.resetLoggedPolicies();
        policyExpirationService.checkExpiredPolicies();

        verify(policyRepository, times(2)).findPoliciesExpiringOnDate(today);
    }
}
