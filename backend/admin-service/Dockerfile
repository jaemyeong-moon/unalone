# Stage 1: Build
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY backend/ ./backend/
COPY backend/settings.gradle ./settings.gradle
WORKDIR /app/backend
RUN gradle :admin-service:bootJar -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/admin-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
