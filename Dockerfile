# 1. 베이스 이미지로 OpenJDK 17을 사용
FROM openjdk:17-jdk-alpine

# 2. 애플리케이션 jar 파일 위치 지정
ARG JAR_FILE=build/libs/*.jar

# 3. 컨테이너에 jar 파일 복사
COPY ${JAR_FILE} app.jar

# 4. 애플리케이션 실행 명령어
ENTRYPOINT ["java","-jar","/app.jar"]