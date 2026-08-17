package com.fldb.facilita.auto.domain.repository;

import com.fldb.facilita.auto.domain.entity.InsuranceCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InsuranceRepository extends JpaRepository<InsuranceCompany, UUID> {
}
