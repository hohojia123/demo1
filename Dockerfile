# 第一阶段：解压 Spring Boot 分层 JAR
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu as builder
WORKDIR /app
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# 第二阶段：运行阶段
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
WORKDIR /app

# 按变更频率从低到高分层 COPY，依赖不变时复用缓存层
COPY --from=builder app/dependencies/ ./
COPY --from=builder app/spring-boot-loader/ ./
COPY --from=builder app/snapshot-dependencies/ ./
COPY --from=builder app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]