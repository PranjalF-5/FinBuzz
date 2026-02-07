# Use official OpenJDK image
FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y curl
# Set work directory inside container
WORKDIR /app

# Copy JAR file into the container
COPY target/registry-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8761


# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
