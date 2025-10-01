package com.algangi.mongle.block.domain.model;

import com.algangi.mongle.global.entity.CreatedDateBaseEntity;
import com.algangi.mongle.member.domain.Member;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

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
