FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar notification-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "notification-service.jar"]