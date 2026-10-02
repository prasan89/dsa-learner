package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.entity.LangCoinPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LangCoinPackageRepository extends JpaRepository<LangCoinPackage, UUID> {

    List<LangCoinPackage> findByIsActiveTrueOrderByDisplayOrderAsc();

    Optional<LangCoinPackage> findByPackageCodeAndIsActiveTrue(String packageCode);
}
