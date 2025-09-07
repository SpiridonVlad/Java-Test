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
import com.example.carins.web.dto.HistoryEventDto;
import com.example.carins.web.dto.UpdateCarDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final OwnerRepository ownerRepository;

    public List<Car> listCars() {
        log.info("Fetching all cars");
        return carRepository.findAll();
    }

    @Transactional
    public Car createCar(CreateCarDto createCarDto) {
        log.info("Creating new car with VIN: {}", createCarDto.vin());

        if (carRepository.findByVin(createCarDto.vin()).isPresent()) {
            throw new ValidationException("Car with VIN " + createCarDto.vin() + " already exists");
        }

        Owner owner = ownerRepository.findById(createCarDto.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + createCarDto.ownerId()));
        
        Car car = new Car(
                createCarDto.vin(),
                createCarDto.make(),
                createCarDto.model(),
                createCarDto.yearOfManufacture(),
                owner
        );
        
        Car savedCar = carRepository.save(car);
        log.info("Successfully created car with id: {}", savedCar.getId());
        return savedCar;
    }

    public Car getCarById(Long carId) {
        log.info("Fetching car with id: {}", carId);
        return carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));
    }

    public List<com.example.carins.web.dto.CarDto> getCarsByOwnerId(Long ownerId) {
        log.info("Fetching cars for owner with id: {}", ownerId);
        List<Car> cars = carRepository.findByOwnerId(ownerId);
        return cars.stream().map(this::toDto).toList();
    }

    private com.example.carins.web.dto.CarDto toDto(Car c) {
        var o = c.getOwner();
        var ownerDto = o != null ? new com.example.carins.web.dto.CarDto.OwnerDto(o.getId(), o.getName(), o.getEmail()) : null;
        return new com.example.carins.web.dto.CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(), ownerDto);
    }

    @Transactional
    public Car updateCar(Long carId, UpdateCarDto updateCarDto) {
        log.info("Updating car with id: {}", carId);
        
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        if (updateCarDto.vin() != null && !updateCarDto.vin().equals(car.getVin())) {
            if (carRepository.findByVin(updateCarDto.vin()).isPresent()) {
                throw new ValidationException("Car with VIN " + updateCarDto.vin() + " already exists");
            }
            car.setVin(updateCarDto.vin());
        }

        if (updateCarDto.make() != null) {
            car.setMake(updateCarDto.make());
        }
        if (updateCarDto.model() != null) {
            car.setModel(updateCarDto.model());
        }
        if (updateCarDto.yearOfManufacture() != null) {
            car.setYearOfManufacture(updateCarDto.yearOfManufacture());
        }
        if (updateCarDto.ownerId() != null) {
            Owner owner = ownerRepository.findById(updateCarDto.ownerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + updateCarDto.ownerId()));
            car.setOwner(owner);
        }

        List<InsurancePolicy> policies = policyRepository.findByCarId(carId);
        if (!policies.isEmpty()) {
            log.info("Deleting {} insurance policies for car id: {}", policies.size(), carId);
            policyRepository.deleteAll(policies);
        }
        
        Car savedCar = carRepository.save(car);
        log.info("Successfully updated car with id: {}", savedCar.getId());
        return savedCar;
    }

    @Transactional
    public void deleteCar(Long carId) {
        log.info("Deleting car with id: {}", carId);
        
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        List<InsurancePolicy> policies = policyRepository.findByCarId(carId);
        if (!policies.isEmpty()) {
            log.info("Deleting {} insurance policies for car id: {}", policies.size(), carId);
            policyRepository.deleteAll(policies);
        }

        List<Claim> claims = claimRepository.findByCarIdOrderByClaimDateDesc(carId);
        if (!claims.isEmpty()) {
            log.info("Deleting {} claims for car id: {}", claims.size(), carId);
            claimRepository.deleteAll(claims);
        }
        
        carRepository.delete(car);
        log.info("Successfully deleted car with id: {}", carId);
    }

    public boolean isInsuranceValid(Long carId, String dateStr) {
        log.info("Checking insurance validity for car: {} on date: {}", carId, dateStr);

        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car not found with id: " + carId);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Expected format: YYYY-MM-DD");
        }

        LocalDate minDate = LocalDate.of(1900, 1, 1);
        LocalDate maxDate = LocalDate.of(2100, 12, 31);

        if (date.isBefore(minDate) || date.isAfter(maxDate)) {
            throw new ValidationException("Date must be between " + minDate + " and " + maxDate);
        }

        boolean isValid = policyRepository.existsActiveOnDate(carId, date);
        log.info("Insurance validity for car {} on date {}: {}", carId, date, isValid);
        return isValid;
    }

    public CarHistoryDto getCarHistory(Long carId) {
        log.info("Fetching history for car: {}", carId);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        List<HistoryEventDto> events = new ArrayList<>();

        List<InsurancePolicy> policies = policyRepository.findByCarId(carId);
        for (InsurancePolicy policy : policies) {
            events.add(new HistoryEventDto(
                    "INSURANCE_POLICY",
                    policy.getStartDate(),
                    String.format("Insurance policy started with %s (valid until %s)",
                            policy.getProvider() != null ? policy.getProvider() : "Unknown Provider",
                            policy.getEndDate()),
                    policy.getStartDate().atStartOfDay()
            ));

            if (policy.getEndDate() != null) {
                events.add(new HistoryEventDto(
                        "INSURANCE_POLICY",
                        policy.getEndDate(),
                        String.format("Insurance policy with %s expired",
                                policy.getProvider() != null ? policy.getProvider() : "Unknown Provider"),
                        policy.getEndDate().atStartOfDay()
                ));
            }
        }

        List<Claim> claims = claimRepository.findByCarIdOrderByClaimDateDesc(carId);
        for (Claim claim : claims) {
            events.add(new HistoryEventDto(
                    "CLAIM",
                    claim.getClaimDate(),
                    String.format("Claim filed: %s (Amount: $%.2f)", claim.getDescription(), claim.getAmount()),
                    claim.getCreatedAt()
            ));
        }

        events.sort(Comparator.comparing(HistoryEventDto::date)
                .thenComparing(HistoryEventDto::timestamp));

        return new CarHistoryDto(
                car.getId(),
                car.getVin(),
                car.getMake(),
                car.getModel(),
                car.getYearOfManufacture(),
                events
        );
    }
}
