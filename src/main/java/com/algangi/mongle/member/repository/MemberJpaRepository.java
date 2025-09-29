package com.algangi.mongle.member.repository;

import com.algangi.mongle.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberJpaRepository extends JpaRepository<Member, String> {

    List<Member> findAllByMemberIdIn(List<String> memberIds);

}