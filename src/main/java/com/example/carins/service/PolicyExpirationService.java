package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class PolicyExpirationService {

    private final InsurancePolicyRepository policyRepository;
    private final Set<Long> alreadyLoggedPolicies = new HashSet<>();

    public PolicyExpirationService(InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Scheduled(fixedRate = 3600000)
    public void checkExpiredPolicies() {
        log.debug("Checking for expired policies...");

        LocalDate today = LocalDate.now();
        List<InsurancePolicy> expiredPolicies = policyRepository.findPoliciesExpiringOnDate(today);

        for (InsurancePolicy policy : expiredPolicies) {
            if (!alreadyLoggedPolicies.contains(policy.getId())) {
                log.info("Policy {} for car {} expired on {}",
                        policy.getId(),
                        policy.getCar().getId(),
                        policy.getEndDate());
                alreadyLoggedPolicies.add(policy.getId());
            }
        }

        if (!expiredPolicies.isEmpty()) {
            log.info("Found {} expired policies for {}", expiredPolicies.size(), today);
        }
    }

    public void resetLoggedPolicies() {
        alreadyLoggedPolicies.clear();
    }
}
