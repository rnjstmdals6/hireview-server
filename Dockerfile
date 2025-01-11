FROM openjdk:17-jdk-alpine

RUN apk add libwebp-tools

ENV SCRIMAGE_CWEBP_PATH="/usr/bin/cwebp"

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]