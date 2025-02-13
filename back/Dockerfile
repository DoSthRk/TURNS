# 使用 OpenJDK 作为基础镜像
FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY target/back-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]