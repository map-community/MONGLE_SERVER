package com.algangi.mongle.member.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algangi.mongle.member.domain.model.SocialAccount;
import com.algangi.mongle.member.domain.model.SocialId;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findBySocialId(SocialId socialId);

}
