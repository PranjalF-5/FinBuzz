FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar gateway-service.jar
EXPOSE 8111
ENTRYPOINT ["java", "-jar", "gateway-service.jar"]