package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CfDomainRepository extends JpaRepository<CfDomain, UUID> {
    Optional<CfDomain> findByCode(String code);
}
