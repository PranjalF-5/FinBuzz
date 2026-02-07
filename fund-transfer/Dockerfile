FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar fund-transfer-service.jar
EXPOSE 7076
ENTRYPOINT ["java", "-jar", "fund-transfer-service.jar"]