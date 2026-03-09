# Use a lightweight Java 17 runtime
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven-built JAR into the container
COPY target/discordcoins-1.0.jar app.jar

# Expose the port (Render will set PORT env)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java","-jar","app.jar"]