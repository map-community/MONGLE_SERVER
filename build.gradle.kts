plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.algangi"
version = "0.0.1-SNAPSHOT"
description = "map based community project"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("software.amazon.awssdk:bom:2.25.41")
    }
}

dependencies {
    //spring data jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //jdbc
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    //validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    //web
    implementation("org.springframework.boot:spring-boot-starter-web")
    //s2
    implementation("io.sgr:s2-geometry-library-java:1.0.1") {
        exclude(group = "com.google.guava", module = "guava")
    }
    implementation("com.google.guava:guava:32.0.1-jre")
    //lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    //mysql
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    //queryDSL
    implementation("io.github.openfeign.querydsl:querydsl-jpa:6.10.1")
    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:6.10.1:jpa")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    // JPA 편의 기능
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.11.0")
    //aws cloud
    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.1.1")
    // AWS CloudFront 연동을 위한 SDK
    implementation("software.amazon.awssdk:cloudfront")
    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // shedlock
    implementation("net.javacrumbs.shedlock:shedlock-spring:6.10.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.10.0")
    //spring security
    implementation("org.springframework.boot:spring-boot-starter-security")
    //jwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-gson:0.12.6")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
