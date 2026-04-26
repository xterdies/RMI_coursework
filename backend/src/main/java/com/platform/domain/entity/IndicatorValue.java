package com.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "indicator_values",
       uniqueConstraints = @UniqueConstraint(columnNames = {"region_id", "indicator_id", "year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IndicatorValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private EconomicIndicator indicator;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal value;

    @Column(length = 512)
    private String sourceUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
