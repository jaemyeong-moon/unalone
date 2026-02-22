FROM gradle:8-jdk17 AS build

WORKDIR /app

COPY backend/ backend/

WORKDIR /app/backend

RUN gradle :event-service:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/backend/event-service/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
