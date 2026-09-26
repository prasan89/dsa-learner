package com.dsalearner.repository;

import com.dsalearner.model.entity.UserLearningDomain;
import com.dsalearner.model.entity.UserLearningDomainId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserLearningDomainRepository
        extends JpaRepository<UserLearningDomain, UserLearningDomainId> {

    @Query(value = "SELECT * FROM user_learning_domains WHERE user_id = CAST(:userId AS uuid)", nativeQuery = true)
    List<UserLearningDomain> findByUserId(@Param("userId") String userId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user_learning_domains WHERE user_id = CAST(:userId AS uuid) AND domain_code = :domainCode)", nativeQuery = true)
    boolean existsByUserIdAndDomainCode(@Param("userId") String userId, @Param("domainCode") String domainCode);
}
