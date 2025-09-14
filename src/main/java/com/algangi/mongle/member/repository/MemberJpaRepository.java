package com.algangi.mongle.member.repository;

import com.algangi.mongle.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long>{

}