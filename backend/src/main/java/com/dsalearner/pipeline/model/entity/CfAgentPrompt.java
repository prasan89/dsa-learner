package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_agent_prompts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfAgentPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String promptKey;

    @Column(nullable = false, length = 60)
    private String agentType;

    @Column(nullable = false, length = 20)
    private String domainCode;

    @Column(length = 10)
    private String languageCode;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, columnDefinition = "text")
    private String promptText;

    @Column(columnDefinition = "text")
    private String systemPrompt;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @CreationTimestamp
    private Instant createdAt;

    private Instant activatedAt;
    private Instant deprecatedAt;

    @Column(length = 100)
    private String createdBy;

    @Column(columnDefinition = "text")
    private String changeNotes;
}
