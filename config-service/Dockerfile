# Use OpenJDK as the base image
FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y curl
# Set working directory
WORKDIR /app

# Copy JAR file into the container
COPY target/*.jar config-service.jar

# Expose the server port
EXPOSE 8888

# Run the application
ENTRYPOINT ["java", "-jar", "config-service.jar"]
