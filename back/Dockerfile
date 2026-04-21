# 使用官方的 OpenJDK 8 镜像
FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /app

# 将 Spring Boot JAR 文件复制到容器内
COPY target/back-0.0.1-SNAPSHOT.jar /app/back.jar

# 暴露端口
EXPOSE 8080

# 启动 Spring Boot 应用
CMD ["java", "-jar", "back.jar"]