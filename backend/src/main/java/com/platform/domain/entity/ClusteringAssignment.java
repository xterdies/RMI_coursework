package com.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "clustering_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"run_id", "region_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClusteringAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private ClusteringRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "cluster_label", nullable = false)
    private Integer clusterLabel;

    @Column(name = "distance_to_centroid", precision = 20, scale = 6)
    private BigDecimal distanceToCentroid;
}
