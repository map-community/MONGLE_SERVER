package com.algangi.mongle.member.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.algangi.mongle.member.domain.model.Member;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface MemberRepository extends JpaRepository<Member, String> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<Member> findByEmail(String email);

    List<Member> findAllByMemberIdIn(List<String> memberIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId")
    Optional<Member> findByIdWithLock(@Param("memberId") String memberId);

}