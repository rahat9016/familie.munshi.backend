
################ BUILD STAGE ################

FROM eclipse-temurin:26-jdk-noble AS builder

WORKDIR /build

# Copy Maven wrapper first for caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw -B dependency:go-offline

# Copy source code
COPY src/ src/

# Package Spring Boot application
RUN ./mvnw -B -DskipTests clean package \
    && cp target/backend-*.jar /build/app.jar


################ RUNTIME STAGE ################

FROM eclipse-temurin:26-jre-noble

# Run as non-root user
RUN groupadd --system --gid 1001 familie \
    && useradd --system \
        --uid 1001 \
        --gid 1001 \
        --shell /usr/sbin/nologin familie

WORKDIR /app

COPY --from=builder \
    --chown=familie:familie \
    /build/app.jar /app/app.jar

USER 1001

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
