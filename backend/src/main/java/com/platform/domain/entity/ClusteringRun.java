package com.platform.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "clustering_runs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClusteringRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "k_clusters", nullable = false)
    private Integer kClusters;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, length = 50)
    private String algorithm = "KMEANS";

    private Integer iterations;

    @Column(precision = 20, scale = 6)
    private BigDecimal inertia;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClusteringAssignment> assignments = new ArrayList<>();
}
