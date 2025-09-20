package com.algangi.mongle.dynamicCloud.domain.model;

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
    private DynamicCloudStatus status = DynamicCloudStatus.ACTIVE;

    @ElementCollection
    @CollectionTable(name = "s2_dynamic_cloud_cells",
        joinColumns = @JoinColumn(name = "s2_token_id", unique = true)
    )
    private Set<String> s2TokenIds;

    public static DynamicCloud create(Set<String> s2TokenIds) {
        return DynamicCloud.builder().build();
    }

    public void mergeWith(DynamicCloud other) {
        if (other.status != DynamicCloudStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태가 아닌 동적구름은 병합될 수 없습니다.");
        }

        this.s2TokenIds.addAll(other.s2TokenIds);
        other.s2TokenIds.clear();
        other.markAsMerged();
    }

    public void markAsMerged() {
        this.status = DynamicCloudStatus.MERGED;
    }

    public boolean containsCell(String s2TokenId) {
        return s2TokenIds.contains(s2TokenId);
    }

}
