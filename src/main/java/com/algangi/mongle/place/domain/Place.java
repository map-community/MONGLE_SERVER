package com.algangi.mongle.place.domain;

import java.util.ArrayList;
import java.util.List;

import com.algangi.mongle.cell.domain.S2Cell;

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
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double centerLatitude;

    @Column(nullable = false)
    private Double centerLongitude;

    @OneToMany(mappedBy = "place", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<S2Cell> s2Cells = new ArrayList<>();

    public static Place createPlace(Double centerLatitude, Double centerLongitude,
        List<S2Cell> s2Cells) {
        Place place = Place.builder()
            .centerLatitude(centerLatitude)
            .centerLongitude(centerLongitude)
            .build();

        place.addS2Cells(s2Cells);

        return place;
    }

    public void addS2Cells(List<S2Cell> s2Cells) {
        s2Cells.forEach(this::addS2Cell);
    }

    public void addS2Cell(S2Cell s2Cell) {
        this.s2Cells.add(s2Cell);
        s2Cell.setPlace(this);
    }

}
