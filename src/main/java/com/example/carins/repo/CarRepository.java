package com.example.carins.repo;

import com.example.carins.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    @EntityGraph(attributePaths = {"owner"})
    List<Car> findAll();
    Optional<Car> findByVin(String vin);
    List<Car> findByOwnerId(Long ownerId);
}