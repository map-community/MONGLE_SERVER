package com.algangi.mongle.s2.domain;

import com.algangi.mongle.dynamicCloud.domain.DynamicCloud;
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
@Table(name = "dynamic_cloud_s2_cell")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class DynamicCloudS2Cell {

    @Id
    @Column(name = "s2_token_id", length = 16, nullable = false)
    private String s2TokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloud_id", nullable = true)
    private DynamicCloud dynamicCloud;

    public static DynamicCloudS2Cell of(String s2TokenId, DynamicCloud dynamicCloud) {
        return DynamicCloudS2Cell.builder()
            .s2TokenId(s2TokenId)
            .dynamicCloud(dynamicCloud)
            .build();
    }
}
