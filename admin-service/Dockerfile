FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar admin-service.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "admin-service.jar"]