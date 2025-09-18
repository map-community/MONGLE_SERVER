package com.algangi.mongle.dynamicCloud.domain;

import java.util.ArrayList;
import java.util.List;

import com.algangi.mongle.global.entity.CreatedDateBaseEntity;
import com.algangi.mongle.s2.domain.DynamicCloudS2Cell;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "dynamicCloud", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<DynamicCloudS2Cell> s2Cells = new ArrayList<>();

    public static DynamicCloud createDynamicCloudFromTokens(List<String> s2TokenIds) {
        DynamicCloud dc = DynamicCloud.builder().build();
        s2TokenIds.forEach(dc::addS2CellToken);
        return dc;
    }

    public void addS2Cells(List<DynamicCloudS2Cell> s2Cells) {
        s2Cells.forEach(this::addS2Cell);
    }

    public void addS2CellToken(String s2TokenId) {
        this.s2Cells.add(DynamicCloudS2Cell.of(s2TokenId, this));
    }

    public void addS2Cell(DynamicCloudS2Cell s2Cell) {
        this.s2Cells.add(s2Cell);
    }
}
