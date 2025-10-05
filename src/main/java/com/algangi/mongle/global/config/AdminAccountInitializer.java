package com.algangi.mongle.global.config;

import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class AdminAccountInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Value("${ADMIN_INITIAL_PASSWORD}")
    private String adminInitialPassword;

    protected record AdminSeed(String id, String email, String nickname) {}

    private static final List<AdminSeed> ADMIN_SEEDS = List.of(
            new AdminSeed("admin_01", "test@test1.com", "관리자1"),
            new AdminSeed("admin_02", "test@test2.com", "관리자2"),
            new AdminSeed("admin_03", "test@test3.com", "관리자3"),
            new AdminSeed("admin_04", "test@test4.com", "관리자4"),
            new AdminSeed("admin_05", "test@test5.com", "관리자5")
    );

    @SchedulerLock(name = "adminAccountInit", lockAtMostFor = "PT5M")
    @Override
    public void run(String... args) {
        log.info("관리자 계정 초기화를 시작합니다...");

        ADMIN_SEEDS.forEach(this::createAdminIfNotExists);

        log.info("관리자 계정 초기화가 완료되었습니다.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void createAdminIfNotExists(AdminSeed admin) {
        if (!memberRepository.existsByEmail(admin.email()) &&
                        !memberRepository.existsById(admin.id())) {
            Member adminMember = Member.createAdmin(
                    admin.id(),
                    admin.email(),
                    admin.nickname(),
                    null,
                    passwordEncoder.encode(adminInitialPassword)
            );
            memberRepository.saveAndFlush(adminMember);
            log.info("'{}' 관리자 계정 생성 완료 (ID: {})", admin.nickname(), admin.id());
        } else {
            log.info("'{}' 관리자 계정이 이미 존재합니다. (ID: {} 또는 Email: {})",
                                     admin.nickname(), admin.id(), admin.email());
        }
    }
}