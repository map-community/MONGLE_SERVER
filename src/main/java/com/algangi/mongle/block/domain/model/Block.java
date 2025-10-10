package com.algangi.mongle.block.domain.model;

import com.algangi.mongle.global.entity.CreatedDateBaseEntity;
import com.algangi.mongle.member.domain.model.Member;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "block", uniqueConstraints = {
    @UniqueConstraint(
        name = "block_uk",
        columnNames = {"blocker_id", "blocked_id"}
    )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Block extends CreatedDateBaseEntity {

    @Id
    @Tsid
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocker_id", nullable = false)
    private Member blocker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_id", nullable = false)
    private Member blocked;

    public static Block of(Member blocker, Member blocked) {
        return Block.builder()
            .blocker(blocker)
            .blocked(blocked)
            .build();
    }
}
