package com.example.carins.service;

import com.example.carins.exception.ResourceNotFoundException;
import com.example.carins.exception.ValidationException;
import com.example.carins.model.Car;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.web.dto.CreateOwnerDto;
import com.example.carins.web.dto.UpdateOwnerDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final CarRepository carRepository;

    public List<Owner> listOwners() {
        log.info("Fetching all owners");
        return ownerRepository.findAll();
    }

    public Owner getOwnerById(Long ownerId) {
        log.info("Fetching owner with id: {}", ownerId);
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
    }

    @Transactional
    public Owner createOwner(CreateOwnerDto createOwnerDto) {
        log.info("Creating new owner with email: {}", createOwnerDto.email());

        if (ownerRepository.existsByEmail(createOwnerDto.email())) {
            throw new ValidationException("Owner with email " + createOwnerDto.email() + " already exists");
        }
        
        Owner owner = new Owner(createOwnerDto.name(), createOwnerDto.email());
        Owner savedOwner = ownerRepository.save(owner);
        
        log.info("Successfully created owner with id: {}", savedOwner.getId());
        return savedOwner;
    }

    @Transactional
    public Owner updateOwner(Long ownerId, UpdateOwnerDto updateOwnerDto) {
        log.info("Updating owner with id: {}", ownerId);
        
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));

        if (updateOwnerDto.email() != null && !updateOwnerDto.email().equals(owner.getEmail())) {
            if (ownerRepository.existsByEmail(updateOwnerDto.email())) {
                throw new ValidationException("Owner with email " + updateOwnerDto.email() + " already exists");
            }
            owner.setEmail(updateOwnerDto.email());
        }

        if (updateOwnerDto.name() != null) {
            owner.setName(updateOwnerDto.name());
        }
        
        Owner savedOwner = ownerRepository.save(owner);
        log.info("Successfully updated owner with id: {}", savedOwner.getId());
        return savedOwner;
    }

    @Transactional
    public void deleteOwner(Long ownerId) {
        log.info("Deleting owner with id: {}", ownerId);
        
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));

        List<Car> cars = carRepository.findByOwnerId(ownerId);
        if (!cars.isEmpty()) {
            throw new ValidationException("Cannot delete owner with id " + ownerId + 
                    " because they own " + cars.size() + " car(s). Please reassign or delete the cars first.");
        }
        
        ownerRepository.delete(owner);
        log.info("Successfully deleted owner with id: {}", ownerId);
    }
}
