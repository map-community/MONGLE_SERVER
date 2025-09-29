package com.algangi.mongle.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algangi.mongle.member.domain.SocialAccount;
import com.algangi.mongle.member.domain.SocialId;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findBySocialId(SocialId socialId);

}
