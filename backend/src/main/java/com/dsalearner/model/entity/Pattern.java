package com.dsalearner.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "patterns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pattern {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String recognitionClues;

    @Column(columnDefinition = "TEXT")
    private String templateCode;

    @Column(nullable = false)
    private int displayOrder;
}
