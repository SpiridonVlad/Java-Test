package com.example.carins.service;

import com.example.carins.exception.ResourceNotFoundException;
import com.example.carins.exception.ValidationException;
import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.web.dto.CarHistoryDto;
import com.example.carins.web.dto.CreateCarDto;
import com.example.carins.web.dto.UpdateCarDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;
    
    @Mock
    private InsurancePolicyRepository policyRepository;
    
    @Mock
    private ClaimRepository claimRepository;
    
    @Mock
    private OwnerRepository ownerRepository;
    
    private CarService carService;
    
    @BeforeEach
    void setUp() {
        carService = new CarService(carRepository, policyRepository, claimRepository, ownerRepository);
    }
    
    @Test
    void listCars_ReturnsAllCars() {
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car1 = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        Car car2 = new Car("VIN456", "Honda", "Civic", 2021, owner);
        when(carRepository.findAll()).thenReturn(List.of(car1, car2));

        List<Car> result = carService.listCars();

        assertEquals(2, result.size());
        verify(carRepository).findAll();
    }
    
    @Test
    void createCar_ValidData_ReturnsCreatedCar() {
        Owner owner = new Owner("John Doe", "john@example.com");
        CreateCarDto dto = new CreateCarDto("VIN123", "Toyota", "Camry", 2020, 1L);
        Car savedCar = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        savedCar.setId(1L);
        
        when(carRepository.findByVin("VIN123")).thenReturn(Optional.empty());
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(carRepository.save(any(Car.class))).thenReturn(savedCar);

        Car result = carService.createCar(dto);

        assertEquals("VIN123", result.getVin());
        assertEquals("Toyota", result.getMake());
        assertEquals(1L, result.getId());
        verify(carRepository).findByVin("VIN123");
        verify(ownerRepository).findById(1L);
        verify(carRepository).save(any(Car.class));
    }
    
    @Test
    void createCar_DuplicateVin_ThrowsValidationException() {
        CreateCarDto dto = new CreateCarDto("VIN123", "Toyota", "Camry", 2020, 1L);
        Car existingCar = new Car("VIN123", "Honda", "Civic", 2019, null);
        
        when(carRepository.findByVin("VIN123")).thenReturn(Optional.of(existingCar));

        ValidationException exception = assertThrows(ValidationException.class, 
                () -> carService.createCar(dto));
        assertEquals("Car with VIN VIN123 already exists", exception.getMessage());
        verify(carRepository).findByVin("VIN123");
        verify(carRepository, never()).save(any());
    }
    
    @Test
    void createCar_OwnerNotFound_ThrowsResourceNotFoundException() {
        CreateCarDto dto = new CreateCarDto("VIN123", "Toyota", "Camry", 2020, 999L);
        
        when(carRepository.findByVin("VIN123")).thenReturn(Optional.empty());
        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> carService.createCar(dto));
        assertEquals("Owner not found with id: 999", exception.getMessage());
    }
    
    @Test
    void getCarById_ValidId_ReturnsCar() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        car.setId(carId);
        
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        Car result = carService.getCarById(carId);

        assertEquals(carId, result.getId());
        assertEquals("VIN123", result.getVin());
        verify(carRepository).findById(carId);
    }
    
    @Test
    void getCarById_InvalidId_ThrowsResourceNotFoundException() {
        Long carId = 999L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> carService.getCarById(carId));
        assertEquals("Car not found with id: 999", exception.getMessage());
    }
    
    @Test
    void getCarsByOwnerId_ValidOwnerId_ReturnsCars() {
        Long ownerId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        owner.setId(ownerId);
        Car car1 = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        Car car2 = new Car("VIN456", "Honda", "Civic", 2021, owner);
        
        when(carRepository.findByOwnerId(ownerId)).thenReturn(List.of(car1, car2));

        var result = carService.getCarsByOwnerId(ownerId);

        assertEquals(2, result.size());
        verify(carRepository).findByOwnerId(ownerId);
    }
    
    @Test
    void updateCar_ValidData_ReturnsUpdatedCar() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car existingCar = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        existingCar.setId(carId);
        
        UpdateCarDto updateDto = new UpdateCarDto("VIN456", "Honda", "Civic", 2021, null);
        
        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
        when(carRepository.findByVin("VIN456")).thenReturn(Optional.empty());
        when(policyRepository.findByCarId(carId)).thenReturn(Collections.emptyList());
        when(carRepository.save(any(Car.class))).thenReturn(existingCar);

        Car result = carService.updateCar(carId, updateDto);

        assertEquals("VIN456", result.getVin());
        assertEquals("Honda", result.getMake());
        verify(carRepository).findById(carId);
        verify(carRepository).save(any(Car.class));
    }
    
    @Test
    void updateCar_DuplicateVin_ThrowsValidationException() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car existingCar = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        existingCar.setId(carId);
        
        Car duplicateCar = new Car("VIN456", "Honda", "Civic", 2021, owner);
        duplicateCar.setId(2L);
        
        UpdateCarDto updateDto = new UpdateCarDto("VIN456", null, null, null, null);
        
        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
        when(carRepository.findByVin("VIN456")).thenReturn(Optional.of(duplicateCar));

        ValidationException exception = assertThrows(ValidationException.class, 
                () -> carService.updateCar(carId, updateDto));
        assertEquals("Car with VIN VIN456 already exists", exception.getMessage());
    }
    
    @Test
    void deleteCar_ValidId_DeletesCarAndRelatedData() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        car.setId(carId);
        
        InsurancePolicy policy = new InsurancePolicy(car, "GEICO", LocalDate.now(), LocalDate.now().plusDays(365));
        Claim claim = new Claim(car, LocalDate.now(), "Test claim", new BigDecimal("1000"));
        
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(policyRepository.findByCarId(carId)).thenReturn(List.of(policy));
        when(claimRepository.findByCarIdOrderByClaimDateDesc(carId)).thenReturn(List.of(claim));

        carService.deleteCar(carId);

        verify(carRepository).findById(carId);
        verify(policyRepository).findByCarId(carId);
        verify(policyRepository).deleteAll(List.of(policy));
        verify(claimRepository).findByCarIdOrderByClaimDateDesc(carId);
        verify(claimRepository).deleteAll(List.of(claim));
        verify(carRepository).delete(car);
    }
    
    @Test
    void isInsuranceValid_ValidCarAndDate_ReturnsTrue() {
        Long carId = 1L;
        String dateStr = "2024-06-01";
        LocalDate date = LocalDate.parse(dateStr);
        
        when(carRepository.existsById(carId)).thenReturn(true);
        when(policyRepository.existsActiveOnDate(carId, date)).thenReturn(true);

        boolean result = carService.isInsuranceValid(carId, dateStr);

        assertTrue(result);
        verify(carRepository).existsById(carId);
        verify(policyRepository).existsActiveOnDate(carId, date);
    }
    
    @Test
    void isInsuranceValid_ValidCarNoActivePolicy_ReturnsFalse() {
        Long carId = 1L;
        String dateStr = "2024-06-01";
        LocalDate date = LocalDate.parse(dateStr);
        
        when(carRepository.existsById(carId)).thenReturn(true);
        when(policyRepository.existsActiveOnDate(carId, date)).thenReturn(false);

        boolean result = carService.isInsuranceValid(carId, dateStr);

        assertFalse(result);
        verify(carRepository).existsById(carId);
        verify(policyRepository).existsActiveOnDate(carId, date);
    }
    
    @Test
    void isInsuranceValid_CarNotFound_ThrowsException() {
        Long carId = 999L;
        String dateStr = "2024-06-01";
        
        when(carRepository.existsById(carId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> carService.isInsuranceValid(carId, dateStr));
        assertEquals("Car not found with id: 999", exception.getMessage());
    }
    
    @Test
    void isInsuranceValid_InvalidDateFormat_ThrowsException() {
        Long carId = 1L;
        String invalidDate = "invalid-date";
        
        when(carRepository.existsById(carId)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, 
                () -> carService.isInsuranceValid(carId, invalidDate));
        assertEquals("Invalid date format. Expected format: YYYY-MM-DD", exception.getMessage());
    }
    
    @Test
    void isInsuranceValid_DateTooEarly_ThrowsException() {
        Long carId = 1L;
        String dateStr = "1800-01-01";
        
        when(carRepository.existsById(carId)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, 
                () -> carService.isInsuranceValid(carId, dateStr));
        assertTrue(exception.getMessage().contains("Date must be between"));
    }
    
    @Test
    void isInsuranceValid_DateTooLate_ThrowsException() {
        Long carId = 1L;
        String dateStr = "2200-01-01";
        
        when(carRepository.existsById(carId)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, 
                () -> carService.isInsuranceValid(carId, dateStr));
        assertTrue(exception.getMessage().contains("Date must be between"));
    }
    
    @Test
    void getCarHistory_ValidCar_ReturnsHistory() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        car.setId(carId);
        
        InsurancePolicy policy = new InsurancePolicy(car, "GEICO", 
                                                   LocalDate.of(2024, 1, 1), 
                                                   LocalDate.of(2024, 12, 31));
        
        Claim claim = new Claim(car, LocalDate.of(2024, 6, 1), 
                               "Minor accident", new BigDecimal("1500.00"));
        claim.setCreatedAt(LocalDateTime.of(2024, 6, 1, 10, 0));
        
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(policyRepository.findByCarId(carId)).thenReturn(List.of(policy));
        when(claimRepository.findByCarIdOrderByClaimDateDesc(carId)).thenReturn(List.of(claim));

        CarHistoryDto result = carService.getCarHistory(carId);

        assertEquals(carId, result.carId());
        assertEquals("VIN123", result.vin());
        assertEquals("Toyota", result.make());
        assertEquals("Camry", result.model());
        assertEquals(2020, result.yearOfManufacture());
        assertFalse(result.events().isEmpty());
        
        verify(carRepository).findById(carId);
        verify(policyRepository).findByCarId(carId);
        verify(claimRepository).findByCarIdOrderByClaimDateDesc(carId);
    }
    
    @Test
    void getCarHistory_CarNotFound_ThrowsException() {
        Long carId = 999L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> carService.getCarHistory(carId));
        assertEquals("Car not found with id: 999", exception.getMessage());
    }
    
    @Test
    void getCarHistory_EmptyHistory_ReturnsHistoryWithEmptyEvents() {
        Long carId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        Car car = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        car.setId(carId);
        
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(policyRepository.findByCarId(carId)).thenReturn(Collections.emptyList());
        when(claimRepository.findByCarIdOrderByClaimDateDesc(carId)).thenReturn(Collections.emptyList());

        CarHistoryDto result = carService.getCarHistory(carId);

        assertEquals(carId, result.carId());
        assertTrue(result.events().isEmpty());
    }
}
