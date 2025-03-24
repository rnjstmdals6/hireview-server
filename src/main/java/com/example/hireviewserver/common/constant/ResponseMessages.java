package com.example.hireviewserver.common.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseMessages {
    // 4xx - Client Errors
    public static final String INVALID_INPUT = "잘못된 요청입니다.";
    public static final String NOT_FOUND = "리소스를 찾을 수 없습니다.";
    public static final String UNAUTHORIZED = "인증이 필요합니다.";
    public static final String FORBIDDEN = "접근 권한이 없습니다.";

    // 5xx - Server Errors
    public static final String INTERNAL_ERROR = "서버 오류가 발생했습니다.";
    public static final String DATABASE_ERROR = "데이터베이스 오류가 발생했습니다.";
}