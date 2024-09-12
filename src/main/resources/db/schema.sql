-- -- 테이블 생성
CREATE TABLE IF NOT EXISTS question
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    job      VARCHAR(255) NOT NULL,
    question TEXT         NOT NULL
);

CREATE TABLE IF NOT EXISTS feedback
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionId BIGINT NOT NULL ,
    score INT NOT NULL ,
    feedback TEXT         NOT NULL
);

-- 테이블에 데이터 삽입
INSERT INTO question (job, question)
VALUES ('백엔드개발자', 'REST와 SOAP의 차이점은 무엇인가요?'),
       ('백엔드개발자', '의존성 주입(Dependency Injection)의 개념과 그 이점은 무엇인가요?'),
       ('백엔드개발자', '마이크로서비스 아키텍처와 모놀리틱 아키텍처의 차이점은 무엇인가요?'),
       ('백엔드개발자', 'RESTful API에서 인증(Authentication)과 인가(Authorization)를 구현하는 방법은?'),
       ('백엔드개발자', '백엔드 개발에서 리포지토리 패턴의 목적은 무엇인가요?'),
       ('백엔드개발자', 'SQL과 NoSQL 데이터베이스의 차이점은 무엇인가요?'),
       ('백엔드개발자', '자바에서 가비지 컬렉션(Garbage Collection)이 어떻게 작동하나요?'),
       ('백엔드개발자', '일반적인 HTTP 상태 코드와 그 의미는 무엇인가요?'),
       ('백엔드개발자', '프로덕션 환경에서 데이터베이스 마이그레이션을 어떻게 처리하나요?'),
       ('백엔드개발자', '트랜잭션이란 무엇이며, 백엔드 개발에서 이를 어떻게 관리하나요?');
