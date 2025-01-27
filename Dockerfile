# Use the official Gradle image to build the application
FROM gradle:7.2.0-jdk17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Download the dependencies
RUN ./gradlew build --no-daemon -x test

# Copy the source code into the container
COPY src ./src

# Package the application
RUN ./gradlew clean build -x test --no-daemon

# Use the official OpenJDK image to run the application
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged jar file from the build stage
COPY --from=build /app/build/libs/balance-service-1.0-SNAPSHOT.jar balance-service.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "balance-service.jar"]
