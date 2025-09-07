package com.example.carins.service;

import com.example.carins.exception.ResourceNotFoundException;
import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.web.dto.ClaimCreateDto;
import com.example.carins.web.dto.ClaimResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;
    
    @Mock
    private CarRepository carRepository;
    
    private ClaimService claimService;
    
    @BeforeEach
    void setUp() {
        claimService = new ClaimService(claimRepository, carRepository);
    }
    
    @Test
    void createClaim_ValidData_Success() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        car.setId(carId);
        
        ClaimCreateDto createDto = new ClaimCreateDto(
            LocalDate.of(2024, 6, 1),
            "Minor accident",
            new BigDecimal("1500.00")
        );
        
        Claim savedClaim = new Claim(car, createDto.claimDate(), createDto.description(), createDto.amount());
        savedClaim.setId(1L);
        savedClaim.setCreatedAt(LocalDateTime.now());
        
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        ClaimResponseDto result = claimService.createClaim(carId, createDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(carId, result.carId());
        assertEquals(createDto.claimDate(), result.claimDate());
        assertEquals(createDto.description(), result.description());
        assertEquals(createDto.amount(), result.amount());
        assertNotNull(result.createdAt());
        
        verify(carRepository).findById(carId);
        verify(claimRepository).save(any(Claim.class));
    }
    
    @Test
    void createClaim_CarNotFound_ThrowsException() {
        Long carId = 999L;
        ClaimCreateDto createDto = new ClaimCreateDto(
            LocalDate.of(2024, 6, 1),
            "Minor accident",
            new BigDecimal("1500.00")
        );
        
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> claimService.createClaim(carId, createDto));
        assertEquals("Car not found with id: 999", exception.getMessage());
        
        verify(carRepository).findById(carId);
        verify(claimRepository, never()).save(any());
    }
    
    @Test
    void createClaim_WithNullAmount_HandlesCorrectly() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        car.setId(carId);
        
        ClaimCreateDto createDto = new ClaimCreateDto(
            LocalDate.of(2024, 6, 1),
            "Minor accident",
            null
        );
        
        Claim savedClaim = new Claim(car, createDto.claimDate(), createDto.description(), null);
        savedClaim.setId(1L);
        savedClaim.setCreatedAt(LocalDateTime.now());
        
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        ClaimResponseDto result = claimService.createClaim(carId, createDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertNull(result.amount());
        
        verify(carRepository).findById(carId);
        verify(claimRepository).save(any(Claim.class));
    }
    
    @Test
    void getClaimsByCarId_ValidCar_ReturnsClaims() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        car.setId(carId);
        
        Claim claim1 = new Claim(car, LocalDate.of(2024, 6, 1), "Accident 1", new BigDecimal("1500.00"));
        claim1.setId(1L);
        claim1.setCreatedAt(LocalDateTime.now());
        
        Claim claim2 = new Claim(car, LocalDate.of(2024, 5, 1), "Accident 2", new BigDecimal("2000.00"));
        claim2.setId(2L);
        claim2.setCreatedAt(LocalDateTime.now());
        
        when(carRepository.existsById(carId)).thenReturn(true);
        when(claimRepository.findByCarIdOrderByClaimDateDesc(carId)).thenReturn(List.of(claim1, claim2));

        List<ClaimResponseDto> result = claimService.getClaimsByCarId(carId);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
        assertEquals(carId, result.get(0).carId());
        assertEquals(carId, result.get(1).carId());
        
        verify(carRepository).existsById(carId);
        verify(claimRepository).findByCarIdOrderByClaimDateDesc(carId);
    }
    
    @Test
    void getClaimsByCarId_CarNotFound_ThrowsException() {
        Long carId = 999L;
        when(carRepository.existsById(carId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> claimService.getClaimsByCarId(carId));
        assertEquals("Car not found with id: 999", exception.getMessage());
        
        verify(carRepository).existsById(carId);
        verify(claimRepository, never()).findByCarIdOrderByClaimDateDesc(any());
    }
    
    @Test
    void getClaimsByCarId_NoClaims_ReturnsEmptyList() {
        Long carId = 1L;
        when(carRepository.existsById(carId)).thenReturn(true);
        when(claimRepository.findByCarIdOrderByClaimDateDesc(carId)).thenReturn(Collections.emptyList());

        List<ClaimResponseDto> result = claimService.getClaimsByCarId(carId);

        assertTrue(result.isEmpty());
        verify(carRepository).existsById(carId);
        verify(claimRepository).findByCarIdOrderByClaimDateDesc(carId);
    }
    
    @Test
    void createClaim_WithZeroAmount_HandlesCorrectly() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        car.setId(carId);
        
        ClaimCreateDto createDto = new ClaimCreateDto(
            LocalDate.of(2024, 6, 1),
            "No damage claim",
            BigDecimal.ZERO
        );
        
        Claim savedClaim = new Claim(car, createDto.claimDate(), createDto.description(), BigDecimal.ZERO);
        savedClaim.setId(1L);
        savedClaim.setCreatedAt(LocalDateTime.now());
        
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        ClaimResponseDto result = claimService.createClaim(carId, createDto);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.amount());
        
        verify(carRepository).findById(carId);
        verify(claimRepository).save(any(Claim.class));
    }
}
