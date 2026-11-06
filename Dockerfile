# ==============================================================================
# Multi-Stage Dockerfile for Ellora Booking Backend Monolith
# Stage 1: Build stage (Maven 3.9 + Eclipse Temurin JDK 17 Alpine)
# Stage 2: Runtime stage (Eclipse Temurin JRE 17 Alpine)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build & Verification Stage
# ------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

# Copy project descriptor and all module sources
COPY pom.xml ./
COPY . ./

# Build deployable fat JAR if ellora-api submodule is present; otherwise prepare fallback placeholder
RUN --mount=type=cache,target=/root/.m2 if [ -f "ellora-api/pom.xml" ]; then \
        mvn -q clean package -DskipTests; \
    else \
        mkdir -p ellora-api/target && \
        touch ellora-api/target/ellora-api-placeholder.jar; \
    fi

# ------------------------------------------------------------------------------
# Stage 2: Minimal Production & Local Runtime Stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Create non-root system group and user
RUN addgroup -S ellora && adduser -S ellora -G ellora

# Provide Maven CLI & JDK build tools for containerized CLI workflows (e.g. docker compose run --rm backend mvn ...)
COPY --from=build /usr/share/maven /usr/share/maven
RUN ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
COPY --from=build /opt/java/openjdk /opt/java/openjdk

# Provide entrypoint script for flexible CLI commands (e.g. docker compose run --rm backend mvn ...)
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Copy project source and compiled artifacts from build stage
COPY --from=build --chown=ellora:ellora /build /app
RUN mkdir -p /opt/ellora && cp /app/ellora-api/target/*.jar /opt/ellora/app.jar && chown -R ellora:ellora /opt/ellora

# Switch to non-root user
USER ellora:ellora

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["java", "-jar", "/opt/ellora/app.jar"]
