package com.dsalearner.repository;

import com.dsalearner.model.entity.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatternRepository extends JpaRepository<Pattern, UUID> {
    Optional<Pattern> findBySlug(String slug);
    List<Pattern> findByCategoryOrderByDisplayOrder(String category);
}
