package com.example.carins.repo;

import com.example.carins.model.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {

    @Query("select case when count(p) > 0 then true else false end " +
            "from InsurancePolicy p " +
            "where p.car.id = :carId " +
            "and p.startDate <= :date " +
            "and p.endDate >= :date")
    boolean existsActiveOnDate(@Param("carId") Long carId, @Param("date") LocalDate date);

    @Query("SELECT p FROM InsurancePolicy p JOIN FETCH p.car c JOIN FETCH c.owner WHERE p.car.id = :carId")
    List<InsurancePolicy> findByCarIdWithCarAndOwner(@Param("carId") Long carId);

    List<InsurancePolicy> findByCarId(Long carId);

    @Query("SELECT p FROM InsurancePolicy p WHERE p.endDate = :date")
    List<InsurancePolicy> findPoliciesExpiringOnDate(@Param("date") LocalDate date);

    @Query("SELECT p FROM InsurancePolicy p WHERE p.endDate IS NULL")
    List<InsurancePolicy> findOpenEndedPolicies();

    @Query("SELECT p FROM InsurancePolicy p JOIN FETCH p.car c JOIN FETCH c.owner")
    List<InsurancePolicy> findAllWithCarAndOwner();

    @Query("SELECT p FROM InsurancePolicy p JOIN FETCH p.car c JOIN FETCH c.owner WHERE p.id = :id")
    Optional<InsurancePolicy> findByIdWithCarAndOwner(@Param("id") Long id);
}