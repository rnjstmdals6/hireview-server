package com.example.hireviewserver.common.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GeminiLogMessages {
    public static final String GEMINI_STREAM_ERROR = "Gemini 스트리밍 중 에러 발생";
    public static final String GEMINI_STREAM_CANCEL = "Gemini 스트리밍이 클라이언트에 의해 중단됨";
    public static final String GEMINI_CALL_ERROR = "Gemini 구조화 응답 중 에러 발생";
}
