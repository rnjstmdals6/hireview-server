package com.example.hireviewserver.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Job {
    FRONTEND("프론트엔드 기술면접"),
    BACKEND("백엔드 기술면접"),
    AI("AI 기술면접"),
    ANDROID("안드로이드 기술면접"),
    IOS("IOS 기술면접"),
    PM("PM 기술면접"),
    GAME("게임 기술면접"),
    MARKETER("마케터 면접"),
    DESIGNER("디자이너 면접"),
    COMMON("공통 인성면접");

    private final String description;
}
