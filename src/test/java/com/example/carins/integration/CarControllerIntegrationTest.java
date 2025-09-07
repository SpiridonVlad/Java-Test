package com.example.carins.integration;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.model.User;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.repo.UserRepository;
import com.example.carins.web.dto.ClaimCreateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class CarControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private InsurancePolicyRepository policyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        policyRepository.deleteAll();
        carRepository.deleteAll();
        ownerRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("testuser", passwordEncoder.encode("password"), "test@example.com", User.Role.USER);
        userRepository.save(testUser);
    }

    @Test
    void getCars_WithAuthentication_Success() throws Exception {
        Owner owner = new Owner("John Doe", "john@example.com");
        ownerRepository.save(owner);

        Car car = new Car("VIN123456789", "Toyota", "Camry", 2020, owner);
        carRepository.save(car);

        mockMvc.perform(get("/api/cars")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].vin").value("VIN123456789"))
                .andExpect(jsonPath("$[0].make").value("Toyota"))
                .andExpect(jsonPath("$[0].model").value("Camry"));
    }

    @Test
    void getCars_WithoutAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkInsuranceValidity_ValidCarAndDate_Success() throws Exception {
        Owner owner = new Owner("John Doe", "john@example.com");
        ownerRepository.save(owner);

        Car car = new Car("VIN123456789", "Toyota", "Camry", 2020, owner);
        carRepository.save(car);

        InsurancePolicy policy = new InsurancePolicy(car, "GEICO",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
        policyRepository.save(policy);

        mockMvc.perform(get("/api/cars/{carId}/insurance-valid", car.getId())
                        .param("date", "2024-06-01")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId").value(car.getId()))
                .andExpect(jsonPath("$.date").value("2024-06-01"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void checkInsuranceValidity_CarNotFound_NotFound() throws Exception {
        mockMvc.perform(get("/api/cars/{carId}/insurance-valid", 999L)
                        .param("date", "2024-06-01")
                        .with(user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    void checkInsuranceValidity_InvalidDate_BadRequest() throws Exception {
        Owner owner = new Owner("John Doe", "john@example.com");
        ownerRepository.save(owner);

        Car car = new Car("VIN123456789", "Toyota", "Camry", 2020, owner);
        carRepository.save(car);

        mockMvc.perform(get("/api/cars/{carId}/insurance-valid", car.getId())
                        .param("date", "invalid-date")
                        .with(user(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void createClaim_ValidData_Success() throws Exception {
        Owner owner = new Owner("John Doe", "john@example.com");
        ownerRepository.save(owner);

        Car car = new Car("VIN123456789", "Toyota", "Camry", 2020, owner);
        carRepository.save(car);

        ClaimCreateDto claimDto = new ClaimCreateDto(
                LocalDate.of(2024, 6, 1),
                "Minor fender bender",
                new BigDecimal("1500.00")
        );

        mockMvc.perform(post("/api/cars/{carId}/claims", car.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimDto))
                        .with(user(testUser)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.carId").value(car.getId()))
                .andExpect(jsonPath("$.description").value("Minor fender bender"))
                .andExpect(jsonPath("$.amount").value(1500.00));
    }

    @Test
    void createClaim_CarNotFound_NotFound() throws Exception {
        ClaimCreateDto claimDto = new ClaimCreateDto(
                LocalDate.of(2024, 6, 1),
                "Minor fender bender",
                new BigDecimal("1500.00")
        );

        mockMvc.perform(post("/api/cars/{carId}/claims", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimDto))
                        .with(user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    void getCarHistory_ValidCar_Success() throws Exception {
        Owner owner = new Owner("John Doe", "john@example.com");
        ownerRepository.save(owner);

        Car car = new Car("VIN123456789", "Toyota", "Camry", 2020, owner);
        carRepository.save(car);

        InsurancePolicy policy = new InsurancePolicy(car, "GEICO",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
        policyRepository.save(policy);

        mockMvc.perform(get("/api/cars/{carId}/history", car.getId())
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId").value(car.getId()))
                .andExpect(jsonPath("$.vin").value("VIN123456789"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.events").isArray());
    }

    @Test
    void getCarHistory_CarNotFound_NotFound() throws Exception {
        mockMvc.perform(get("/api/cars/{carId}/history", 999L)
                        .with(user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }
}
