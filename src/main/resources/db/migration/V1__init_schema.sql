-- V1__init_schema.sql

-- Create Tables
CREATE TABLE block (
    created_date DATETIME(6) NOT NULL,
    blocked_id VARCHAR(255) NOT NULL,
    blocker_id VARCHAR(255) NOT NULL,
    id VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE comment (
    created_date DATETIME(6) NOT NULL,
    dislike_count BIGINT NOT NULL,
    like_count BIGINT NOT NULL,
    updated_date DATETIME(6) NOT NULL,
    version BIGINT,
    content VARCHAR(255) NOT NULL,
    id VARCHAR(255) NOT NULL,
    member_id VARCHAR(255) NOT NULL,
    parent_comment_id VARCHAR(255),
    post_id VARCHAR(255) NOT NULL,
    status ENUM ('ACTIVE','DELETED_BY_ADMIN','DELETED_BY_USER') NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE dynamic_cloud (
    created_date DATETIME(6) NOT NULL,
    id BIGINT NOT NULL AUTO_INCREMENT,
    status ENUM ('ACTIVE','EXPIRED','MERGED') NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE dynamic_cloud_s2_cell (
    dynamic_cloud_id BIGINT NOT NULL,
    s2_token_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (dynamic_cloud_id, s2_token_id)
) ENGINE=InnoDB;

CREATE TABLE member (
    created_date DATETIME(6) NOT NULL,
    updated_date DATETIME(6) NOT NULL,
    email VARCHAR(255) NOT NULL,
    encoded_password VARCHAR(255) NOT NULL,
    member_id VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    profile_image VARCHAR(255),
    member_role ENUM ('ADMIN','USER') NOT NULL,
    status ENUM ('ACTIVE','BANNED','HUMAN') NOT NULL,
    PRIMARY KEY (member_id)
) ENGINE=InnoDB;

CREATE TABLE post (
    latitude FLOAT(53) NOT NULL,
    longitude FLOAT(53) NOT NULL,
    ranking_score FLOAT(53),
    comment_count BIGINT NOT NULL,
    created_date DATETIME(6) NOT NULL,
    dislike_count BIGINT NOT NULL,
    dynamic_cloud_id BIGINT,
    expired_at DATETIME(6) NOT NULL,
    like_count BIGINT NOT NULL,
    static_cloud_id BIGINT,
    updated_date DATETIME(6) NOT NULL,
    view_count BIGINT NOT NULL,
    author_id VARCHAR(255) NOT NULL,
    content VARCHAR(255) NOT NULL,
    id VARCHAR(255) NOT NULL,
    s2token_id VARCHAR(255) NOT NULL,
    status ENUM ('ACTIVE','DELETED_BY_ADMIN','DELETED_BY_USER','EXPIRED','UPLOADING') NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE post_file (
    file_key VARCHAR(255) NOT NULL,
    id VARCHAR(255) NOT NULL,
    post_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE reaction (
    created_date DATETIME(6) NOT NULL,
    id VARCHAR(255) NOT NULL,
    member_id VARCHAR(255) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    target_type ENUM ('COMMENT','POST') NOT NULL,
    type ENUM ('DISLIKE','LIKE') NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE report (
    created_date DATETIME(6) NOT NULL,
    id VARCHAR(255) NOT NULL,
    reporter_id VARCHAR(255) NOT NULL,
    target_author_id VARCHAR(255) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    reason ENUM ('ABUSE','ILLEGAL','INAPPROPRIATE','PORNOGRAPHY','SPAM') NOT NULL,
    report_status ENUM ('PROCESSED','RECEIVED','REJECTED') NOT NULL,
    target_type ENUM ('COMMENT','POST') NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE social_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(255) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    provider ENUM ('KAKAO') NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE static_cloud (
    latitude FLOAT(53) NOT NULL,
    longitude FLOAT(53) NOT NULL,
    cloud_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (cloud_id)
) ENGINE=InnoDB;

CREATE TABLE static_cloud_s2_cell (
    cloud_id BIGINT NOT NULL,
    s2_token_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (cloud_id, s2_token_id)
) ENGINE=InnoDB;


-- Add Unique Constraints
ALTER TABLE block ADD CONSTRAINT block_uk UNIQUE (blocker_id, blocked_id);
ALTER TABLE dynamic_cloud_s2_cell ADD CONSTRAINT UK7yvy0fqlsgvsqhc1imscfixds UNIQUE (s2_token_id);
ALTER TABLE member ADD CONSTRAINT uk_member_email UNIQUE (email);
ALTER TABLE member ADD CONSTRAINT uk_member_nickname UNIQUE (nickname);
ALTER TABLE reaction ADD CONSTRAINT reaction_uk UNIQUE (member_id, target_id, target_type);
ALTER TABLE report ADD CONSTRAINT uk_report_reporter_target UNIQUE (reporter_id, target_id, target_type);
ALTER TABLE social_account ADD CONSTRAINT uk_social_provider_id UNIQUE (provider, provider_id);
ALTER TABLE static_cloud ADD CONSTRAINT uk_static_cloud_name UNIQUE (name);
ALTER TABLE static_cloud_s2_cell ADD CONSTRAINT UKita3k0loxoq7ge57vfitmojf UNIQUE (s2_token_id);



-- Add Foreign Keys
ALTER TABLE block ADD CONSTRAINT FKs7y2qykjfj3ytfoewwjwjaq7t FOREIGN KEY (blocked_id) REFERENCES member (member_id);
ALTER TABLE block ADD CONSTRAINT FKo3q4wv2uen3degjx9q27gxydt FOREIGN KEY (blocker_id) REFERENCES member (member_id);
ALTER TABLE comment ADD CONSTRAINT FKmrrrpi513ssu63i2783jyiv9m FOREIGN KEY (member_id) REFERENCES member (member_id);
ALTER TABLE comment ADD CONSTRAINT FKhvh0e2ybgg16bpu229a5teje7 FOREIGN KEY (parent_comment_id) REFERENCES comment (id);
ALTER TABLE comment ADD CONSTRAINT FKs1slvnkuemjsq2kj4h3vhx7i1 FOREIGN KEY (post_id) REFERENCES post (id);
ALTER TABLE dynamic_cloud_s2_cell ADD CONSTRAINT FK3otgmk6gtu8wn4o19n3uvdimu FOREIGN KEY (dynamic_cloud_id) REFERENCES dynamic_cloud (id);
ALTER TABLE post_file ADD CONSTRAINT FKn75omflablcagq3jsuoognqwy FOREIGN KEY (post_id) REFERENCES post (id);
ALTER TABLE reaction ADD CONSTRAINT FKf0kgc48u5e6wakvieex07kk5w FOREIGN KEY (member_id) REFERENCES member (member_id);
ALTER TABLE report ADD CONSTRAINT FK1uivt2jamt7slp3banldgnsef FOREIGN KEY (reporter_id) REFERENCES member (member_id);
ALTER TABLE social_account ADD CONSTRAINT FK2bhcb0uq2buq7m4c6ogvo1fx8 FOREIGN KEY (member_id) REFERENCES member (member_id);
ALTER TABLE static_cloud_s2_cell ADD CONSTRAINT FKmqbkacdv485iq2aboykkkpjeo FOREIGN KEY (cloud_id) REFERENCES static_cloud (cloud_id);
ALTER TABLE post ADD CONSTRAINT fk_post_dynamic_cloud FOREIGN KEY (dynamic_cloud_id) REFERENCES dynamic_cloud (id);
ALTER TABLE post ADD CONSTRAINT fk_post_static_cloud FOREIGN KEY (static_cloud_id) REFERENCES static_cloud (cloud_id);
ALTER TABLE post ADD CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES member (member_id);



-- Add Indexes
CREATE INDEX idx_post_s2token ON post (s2token_id);