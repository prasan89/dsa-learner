package com.dsalearner.economy.analytics;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "langoa_economy_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LangoaEconomyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 60)
    private String eventName;

    private UUID userId;

    @Column(length = 10)
    private String languageCode;

    private Long amount;

    @Column(length = 20)
    private String currencyType;

    @Column(length = 100)
    private String sourceRef;

    @Column(length = 100)
    private String correlationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> extra;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
