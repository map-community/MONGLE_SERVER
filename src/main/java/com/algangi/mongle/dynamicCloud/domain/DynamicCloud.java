package com.algangi.mongle.dynamicCloud.domain;

import java.util.ArrayList;
import java.util.List;

import com.algangi.mongle.cell.domain.S2Cell;
import com.algangi.mongle.global.entity.CreatedDateBaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private DynamicCloudStatus dynamicCloudStatus = DynamicCloudStatus.ACTIVE;

    @OneToMany(mappedBy = "dynamicCloud", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<S2Cell> s2Cells = new ArrayList<>();

    public static DynamicCloud createDynamicCloud(List<S2Cell> s2Cells){
        DynamicCloud dynamicCloud = DynamicCloud.builder()
            .build();

        dynamicCloud.addS2Cells(s2Cells);
        return dynamicCloud;
    }

    public void addS2Cells(List<S2Cell> s2Cells){
        s2Cells.forEach(this::addS2Cell);
    }

    public void addS2Cell(S2Cell s2Cell) {
        this.s2Cells.add(s2Cell);
        s2Cell.setDynamicCloud(this);
    }



}
