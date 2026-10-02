package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaMilestoneReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LangoaMilestoneRewardRepository extends JpaRepository<LangoaMilestoneReward, String> {
    Optional<LangoaMilestoneReward> findByMilestoneType(String milestoneType);
}
