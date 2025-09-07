package com.example.carins.service;

import com.example.carins.exception.ResourceNotFoundException;
import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.web.dto.ClaimCreateDto;
import com.example.carins.web.dto.ClaimResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class ClaimService {


    private final ClaimRepository claimRepository;
    private final CarRepository carRepository;

    public ClaimResponseDto createClaim(Long carId, ClaimCreateDto claimCreateDto) {
        log.info("Creating claim for car: {}", carId);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        Claim claim = new Claim(
                car,
                claimCreateDto.claimDate(),
                claimCreateDto.description(),
                claimCreateDto.amount()
        );

        Claim savedClaim = claimRepository.save(claim);
        log.info("Successfully created claim with id: {} for car: {}", savedClaim.getId(), carId);

        return mapToResponseDto(savedClaim);
    }

    public List<ClaimResponseDto> getClaimsByCarId(Long carId) {
        log.info("Fetching claims for car: {}", carId);

        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car not found with id: " + carId);
        }

        List<Claim> claims = claimRepository.findByCarIdOrderByClaimDateDesc(carId);
        return claims.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private ClaimResponseDto mapToResponseDto(Claim claim) {
        return new ClaimResponseDto(
                claim.getId(),
                claim.getCar().getId(),
                claim.getClaimDate(),
                claim.getDescription(),
                claim.getAmount(),
                claim.getCreatedAt()
        );
    }
}
