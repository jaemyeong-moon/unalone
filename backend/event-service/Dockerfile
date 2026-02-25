# Stage 1: Build
FROM gradle:8-jdk17 AS build
WORKDIR /app

COPY . .

RUN gradle :event-service:bootJar -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/event-service/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
