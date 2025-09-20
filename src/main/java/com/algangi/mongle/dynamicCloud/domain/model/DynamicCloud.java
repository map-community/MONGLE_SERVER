package com.algangi.mongle.dynamicCloud.domain.model;

import java.util.List;
import java.util.Set;

import com.algangi.mongle.global.entity.CreatedDateBaseEntity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dynamic_cloud")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class DynamicCloud extends CreatedDateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private DynamicCloudStatus dynamicCloudStatus = DynamicCloudStatus.ACTIVE;

    @ElementCollection
    @CollectionTable(name = "s2_dynamic_cloud_cells",
        joinColumns = @JoinColumn(name = "s2_token_id", unique = true)
    )
    private Set<String> s2TokenIds;

    public static DynamicCloud createDynamicCloudFromTokens(List<String> s2TokenIds) {
        return DynamicCloud.builder().build();
    }

}
