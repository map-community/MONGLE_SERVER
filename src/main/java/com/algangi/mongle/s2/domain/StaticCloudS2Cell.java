package com.algangi.mongle.s2.domain;

import com.algangi.mongle.staticCloud.domain.StaticCloud;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "static_cloud_s2_cell")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class StaticCloudS2Cell {

    @Id
    @Column(name = "s2_token_id", length = 16, nullable = false)
    private String s2TokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "static_cloud_id", nullable = false)
    private StaticCloud staticCloud;

    public static StaticCloudS2Cell of(String s2TokenId, StaticCloud staticCloud) {
        return StaticCloudS2Cell.builder()
            .s2TokenId(s2TokenId)
            .staticCloud(staticCloud)
            .build();
    }
}
