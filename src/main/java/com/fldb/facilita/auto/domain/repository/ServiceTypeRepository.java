package com.fldb.facilita.auto.domain.repository;

import com.fldb.facilita.auto.domain.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, UUID> {
}
