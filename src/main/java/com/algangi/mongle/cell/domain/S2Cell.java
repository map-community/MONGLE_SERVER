package com.algangi.mongle.cell.domain;

import com.algangi.mongle.dynamicCloud.domain.DynamicCloud;
import com.algangi.mongle.place.domain.Place;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "s2_cell")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class S2Cell {

    @Id
    private String s2CellId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dynamic_cloud_id")
    private DynamicCloud dynamicCloud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    public static S2Cell createS2Cell(String s2CellId){
        return new S2Cell(s2CellId, null, null);
    }

    public void setDynamicCloud(DynamicCloud dynamicCloud) {
        this.dynamicCloud = dynamicCloud;
    }

    public void setPlace(Place place){
        this.place = place;
    }
}
