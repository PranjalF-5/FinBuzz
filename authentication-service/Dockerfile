FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar authentication-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "authentication-service.jar"]