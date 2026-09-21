package com.dsalearner.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "hints", uniqueConstraints = @UniqueConstraint(columnNames = {"problem_id","level"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Hint {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
