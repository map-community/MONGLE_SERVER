package com.algangi.mongle.report.domain.model;

public enum ReportReason {
    SPAM,       // 스팸/홍보성 콘텐츠
    ABUSE,      // 욕설/비방 등 불쾌한 표현
    PORNOGRAPHY,// 음란물/성희롱
    ILLEGAL,    // 불법 정보
    INAPPROPRIATE // 기타 부적절한 콘텐츠
}