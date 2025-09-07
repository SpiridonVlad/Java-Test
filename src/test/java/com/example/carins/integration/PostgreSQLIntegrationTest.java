package com.example.carins.integration;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class PostgreSQLIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("carinsurance_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private InsurancePolicyRepository policyRepository;

    @Autowired
    private CarService carService;

    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
        carRepository.deleteAll();
        ownerRepository.deleteAll();
    }

    @Test
    void testInsuranceValidityWithPostgreSQL() {
        Owner owner = new Owner("John Doe", "john@example.com");
        ownerRepository.save(owner);

        Car car = new Car("VIN123456789", "Toyota", "Camry", 2020, owner);
        carRepository.save(car);

        InsurancePolicy policy = new InsurancePolicy(car, "GEICO",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
        policyRepository.save(policy);

        boolean isValidInRange = carService.isInsuranceValid(car.getId(), "2024-06-01");
        boolean isValidOutOfRange = carService.isInsuranceValid(car.getId(), "2025-06-01");

        assertTrue(isValidInRange);
        assertFalse(isValidOutOfRange);
    }

    @Test
    void testCarHistoryWithPostgreSQL() {
        Owner owner = new Owner("Jane Smith", "jane@example.com");
        ownerRepository.save(owner);

        Car car = new Car("VIN987654321", "Honda", "Civic", 2021, owner);
        carRepository.save(car);

        InsurancePolicy policy1 = new InsurancePolicy(car, "State Farm",
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31));
        policyRepository.save(policy1);

        InsurancePolicy policy2 = new InsurancePolicy(car, "GEICO",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
        policyRepository.save(policy2);

        var history = carService.getCarHistory(car.getId());

        assertNotNull(history);
        assertEquals(car.getId(), history.carId());
        assertEquals("VIN987654321", history.vin());
        assertEquals("Honda", history.make());
        assertEquals("Civic", history.model());
        assertEquals(2021, history.yearOfManufacture());
        assertFalse(history.events().isEmpty());

        assertEquals(4, history.events().size());
    }
}
