FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar transaction-service.jar
EXPOSE 7075
ENTRYPOINT ["java", "-jar", "transaction-service.jar"]