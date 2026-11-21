# syntax=docker/dockerfile:1.7
# ==============================================================================
# Production image — Ellora Booking Backend
# Stage 1: build the executable fat JAR with Maven.
# Stage 2: runtime only (JRE, no Maven, no JDK, no sources).
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1 — build
# ------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

# Build only the API module and the modules it depends on.
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ellora-api -am package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2 — runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Non-root runtime user.
RUN addgroup -S ellora && adduser -S ellora -G ellora

WORKDIR /app

# Only the executable JAR ships in the final image.
COPY --from=build --chown=ellora:ellora /build/ellora-api/target/ellora-api-*.jar /app/app.jar

USER ellora

EXPOSE 8080

# Exec form so the JVM receives SIGTERM and Spring can shut down gracefully.
# MaxRAMPercentage keeps the heap honest when the container limit changes.
ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=75", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Duser.timezone=UTC", \
    "-jar", "/app/app.jar"]
