package com.example.carins.service;

import com.example.carins.exception.ResourceNotFoundException;
import com.example.carins.exception.ValidationException;
import com.example.carins.model.Car;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.web.dto.CreateOwnerDto;
import com.example.carins.web.dto.UpdateOwnerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    private OwnerRepository ownerRepository;
    
    @Mock
    private CarRepository carRepository;
    
    private OwnerService ownerService;
    
    @BeforeEach
    void setUp() {
        ownerService = new OwnerService(ownerRepository, carRepository);
    }
    
    @Test
    void listOwners_ReturnsAllOwners() {
        Owner owner1 = new Owner("John Doe", "john@example.com");
        Owner owner2 = new Owner("Jane Smith", "jane@example.com");
        when(ownerRepository.findAll()).thenReturn(List.of(owner1, owner2));

        List<Owner> result = ownerService.listOwners();

        assertEquals(2, result.size());
        verify(ownerRepository).findAll();
    }
    
    @Test
    void getOwnerById_ValidId_ReturnsOwner() {
        Long ownerId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        owner.setId(ownerId);
        
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Owner result = ownerService.getOwnerById(ownerId);

        assertEquals(ownerId, result.getId());
        assertEquals("John Doe", result.getName());
        verify(ownerRepository).findById(ownerId);
    }
    
    @Test
    void getOwnerById_InvalidId_ThrowsResourceNotFoundException() {
        Long ownerId = 999L;
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> ownerService.getOwnerById(ownerId));
        
        assertEquals("Owner not found with id: 999", exception.getMessage());
        verify(ownerRepository).findById(ownerId);
    }
    
    @Test
    void createOwner_ValidData_ReturnsCreatedOwner() {
        CreateOwnerDto createDto = new CreateOwnerDto("John Doe", "john@example.com");
        Owner savedOwner = new Owner("John Doe", "john@example.com");
        savedOwner.setId(1L);
        
        when(ownerRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(ownerRepository.save(any(Owner.class))).thenReturn(savedOwner);

        Owner result = ownerService.createOwner(createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        
        verify(ownerRepository).existsByEmail("john@example.com");
        verify(ownerRepository).save(any(Owner.class));
    }
    
    @Test
    void createOwner_EmailAlreadyExists_ThrowsValidationException() {
        CreateOwnerDto createDto = new CreateOwnerDto("John Doe", "existing@example.com");
        
        when(ownerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ownerService.createOwner(createDto));
        
        assertEquals("Owner with email existing@example.com already exists", exception.getMessage());
        verify(ownerRepository).existsByEmail("existing@example.com");
        verify(ownerRepository, never()).save(any());
    }
    
    @Test
    void updateOwner_ValidData_ReturnsUpdatedOwner() {
        Long ownerId = 1L;
        Owner existingOwner = new Owner("John Doe", "john@example.com");
        existingOwner.setId(ownerId);
        
        UpdateOwnerDto updateDto = new UpdateOwnerDto("John Smith", "john.smith@example.com");
        
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(existingOwner));
        when(ownerRepository.existsByEmail("john.smith@example.com")).thenReturn(false);
        when(ownerRepository.save(any(Owner.class))).thenReturn(existingOwner);

        Owner result = ownerService.updateOwner(ownerId, updateDto);

        assertEquals("John Smith", existingOwner.getName());
        assertEquals("john.smith@example.com", existingOwner.getEmail());
        
        verify(ownerRepository).findById(ownerId);
        verify(ownerRepository).existsByEmail("john.smith@example.com");
        verify(ownerRepository).save(existingOwner);
    }
    
    @Test
    void updateOwner_OwnerNotFound_ThrowsResourceNotFoundException() {
        Long ownerId = 999L;
        UpdateOwnerDto updateDto = new UpdateOwnerDto("John Smith", "john.smith@example.com");
        
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> ownerService.updateOwner(ownerId, updateDto));
        
        assertEquals("Owner not found with id: 999", exception.getMessage());
        verify(ownerRepository).findById(ownerId);
        verify(ownerRepository, never()).save(any());
    }
    
    @Test
    void updateOwner_EmailAlreadyExists_ThrowsValidationException() {
        Long ownerId = 1L;
        Owner existingOwner = new Owner("John Doe", "john@example.com");
        existingOwner.setId(ownerId);
        
        UpdateOwnerDto updateDto = new UpdateOwnerDto("John Smith", "existing@example.com");
        
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(existingOwner));
        when(ownerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ownerService.updateOwner(ownerId, updateDto));
        
        assertEquals("Owner with email existing@example.com already exists", exception.getMessage());
        verify(ownerRepository).findById(ownerId);
        verify(ownerRepository).existsByEmail("existing@example.com");
        verify(ownerRepository, never()).save(any());
    }
    
    @Test
    void updateOwner_SameEmail_AllowsUpdate() {
        Long ownerId = 1L;
        Owner existingOwner = new Owner("John Doe", "john@example.com");
        existingOwner.setId(ownerId);
        
        UpdateOwnerDto updateDto = new UpdateOwnerDto("John Smith", "john@example.com");
        
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(existingOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(existingOwner);

        Owner result = ownerService.updateOwner(ownerId, updateDto);

        assertEquals("John Smith", existingOwner.getName());
        assertEquals("john@example.com", existingOwner.getEmail());
        
        verify(ownerRepository).findById(ownerId);
        verify(ownerRepository, never()).existsByEmail(any());
        verify(ownerRepository).save(existingOwner);
    }
    
    @Test
    void updateOwner_PartialUpdate_OnlyUpdatesProvidedFields() {
        Long ownerId = 1L;
        Owner existingOwner = new Owner("John Doe", "john@example.com");
        existingOwner.setId(ownerId);
        
        UpdateOwnerDto updateDto = new UpdateOwnerDto("John Smith", null);
        
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(existingOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(existingOwner);

        Owner result = ownerService.updateOwner(ownerId, updateDto);

        assertEquals("John Smith", existingOwner.getName());
        assertEquals("john@example.com", existingOwner.getEmail());
        
        verify(ownerRepository).findById(ownerId);
        verify(ownerRepository).save(existingOwner);
    }
    
    @Test
    void deleteOwner_ValidId_DeletesOwner() {
        Long ownerId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        owner.setId(ownerId);
        
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(carRepository.findByOwnerId(ownerId)).thenReturn(Collections.emptyList());

        ownerService.deleteOwner(ownerId);

        verify(ownerRepository).findById(ownerId);
        verify(carRepository).findByOwnerId(ownerId);
        verify(ownerRepository).delete(owner);
    }
    
    @Test
    void deleteOwner_OwnerNotFound_ThrowsResourceNotFoundException() {
        Long ownerId = 999L;
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> ownerService.deleteOwner(ownerId));
        
        assertEquals("Owner not found with id: 999", exception.getMessage());
        verify(ownerRepository).findById(ownerId);
        verify(ownerRepository, never()).delete(any());
    }
    
    @Test
    void deleteOwner_OwnerHasCars_ThrowsValidationException() {
        Long ownerId = 1L;
        Owner owner = new Owner("John Doe", "john@example.com");
        owner.setId(ownerId);
        
        Car car1 = new Car("VIN123", "Toyota", "Camry", 2020, owner);
        Car car2 = new Car("VIN456", "Honda", "Civic", 2021, owner);
        
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(carRepository.findByOwnerId(ownerId)).thenReturn(List.of(car1, car2));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> ownerService.deleteOwner(ownerId));
        
        assertEquals("Cannot delete owner with id 1 because they own 2 car(s). Please reassign or delete the cars first.", 
                exception.getMessage());
        verify(ownerRepository).findById(ownerId);
        verify(carRepository).findByOwnerId(ownerId);
        verify(ownerRepository, never()).delete(any());
    }
}
