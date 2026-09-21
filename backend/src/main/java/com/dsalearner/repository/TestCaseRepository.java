package com.dsalearner.repository;

import com.dsalearner.model.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestCaseRepository extends JpaRepository<com.dsalearner.model.entity.TestCase, UUID> {
    java.util.List<com.dsalearner.model.entity.TestCase> findAllByProblemOrderByDisplayOrderAsc(Problem problem);
}
