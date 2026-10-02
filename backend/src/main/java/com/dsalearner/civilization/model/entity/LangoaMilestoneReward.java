package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "langoa_milestone_rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaMilestoneReward {

    @Id
    @Column(name = "milestone_type", length = 40)
    private String milestoneType;

    @Column(nullable = false)
    private long coinBonus;

    @Column(length = 200)
    private String description;
}
