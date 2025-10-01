package com.algangi.mongle.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algangi.mongle.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, String> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<Member> findByEmail(String email);

    List<Member> findAllByMemberIdIn(List<String> memberIds);

}