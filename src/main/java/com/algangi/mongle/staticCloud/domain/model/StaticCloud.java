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
@Builder(access = AccessLevel.PRIVATE)
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
    @CollectionTable(
        name = "static_cloud_s2_cell",
        joinColumns = @JoinColumn(name = "cloud_id", referencedColumnName = "cloud_id")
    )
    @Column(name = "s2_token_id", nullable = false, unique = true)
    private Set<String> s2TokenIds;

    public static StaticCloud createStaticCloud(String name, Double latitude, Double longitude, Set<String> s2TokenIds) {
        validateS2TokenIds(s2TokenIds);

        return StaticCloud.builder()
            .name(name)
            .latitude(latitude)
            .longitude(longitude)
            .s2TokenIds(s2TokenIds)
            .build();
    }

    private static void validateS2TokenIds(Set<String> s2TokenIds) {
        if(s2TokenIds == null || s2TokenIds.isEmpty()) {
            throw new IllegalArgumentException("정적 구름 생성 시 S2 Cell 토큰 값이 존재해야합니다.");
        }
    }
}
