package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "cf_workflow_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfWorkflowEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID lessonId;

    private Integer lessonVersion;

    @Column(length = 30)
    private String fromStatus;

    @Column(nullable = false, length = 30)
    private String toStatus;

    @Column(nullable = false, length = 20)
    private String statusType;

    @Column(nullable = false, length = 60)
    private String trigger;

    @Column(length = 100)
    private String actor;

    private UUID agentRunId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(nullable = false)
    private Instant occurredAt;
}
