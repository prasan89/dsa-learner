package com.dsalearner.model.entity;

import com.dsalearner.model.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private String language = "JAVA";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    private Integer runtimeMs;
    private Integer memoryKb;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    private Instant submittedAt;
}
