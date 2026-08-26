package com.fldb.facilita.auto.domain.repository;

import com.fldb.facilita.auto.domain.entity.company.Yard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface YardRepository extends JpaRepository<Yard, UUID> {
}
