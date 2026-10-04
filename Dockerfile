# ==========================================
# Stage 1: Build Application
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and dependency files first
# This improves Docker layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy application source
COPY src ./src

# Build executable JAR
RUN ./mvnw clean package -DskipTests


# ==========================================
# Stage 2: Production Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

# Create non-root user and group
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR and assign ownership directly
COPY --from=builder \
     --chown=appuser:appgroup \
     /app/target/*.jar \
     app.jar

# Run application as non-root user
USER appuser

EXPOSE 8080

# JVM configuration for containers
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]