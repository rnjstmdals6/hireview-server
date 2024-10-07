#!/bin/bash

# Step 1: Gradle Clean Build (Tests 제외)
echo "Running Gradle clean build..."
./gradlew clean build -x test

# Step 2: Docker 이미지 빌드
echo "Building Docker image..."
docker build -t rnjstmdals6/hireview-server:latest .

# Step 3: DockerHub로 이미지 푸시
echo "Pushing Docker image to DockerHub..."
docker push rnjstmdals6/hireview-server:latest

echo "Docker image build and push complete!"
