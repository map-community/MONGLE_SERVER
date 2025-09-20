package com.algangi.mongle.staticCloud.domain.model;

import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "static_cloud",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_static_cloud_name", columnNames = {"name"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class StaticCloud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cloud_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @ElementCollection
    @CollectionTable(name = "s2_static_cloud_cells",
        joinColumns = @JoinColumn(name = "s2_token_id", unique = true)
    )
    private Set<String> s2TokenIds;

    public static StaticCloud createStaticCloud(String name, Double latitude, Double longitude, Set<String> s2TokenIds) {
        return StaticCloud.builder()
            .name(name)
            .latitude(latitude)
            .longitude(longitude)
            .s2TokenIds(s2TokenIds)
            .build();
    }
}
