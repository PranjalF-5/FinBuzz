FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar account-service.jar
EXPOSE 7074
ENTRYPOINT ["java", "-jar", "account-service.jar"]