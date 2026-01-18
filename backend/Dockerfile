# Build stage
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle build --no-daemon || return 0

# Copy source code
COPY src ./src

# Build application
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create non-root user
RUN groupadd -r chatapp && useradd -r -g chatapp chatapp

# Copy built jar
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R chatapp:chatapp /app

USER chatapp

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
