package com.platform.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "trend_models",
       uniqueConstraints = @UniqueConstraint(columnNames = {"region_id", "indicator_id", "forecast_year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrendModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private EconomicIndicator indicator;

    @Column(name = "model_type", nullable = false, length = 50)
    private String modelType = "LINEAR";

    @Column(precision = 20, scale = 8)
    private BigDecimal slope;

    @Column(precision = 20, scale = 8)
    private BigDecimal intercept;

    @Column(name = "r_squared", precision = 8, scale = 6)
    private BigDecimal rSquared;

    @Column(name = "forecast_year", nullable = false)
    private Integer forecastYear;

    @Column(name = "forecast_value", precision = 20, scale = 4)
    private BigDecimal forecastValue;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> parameters;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
