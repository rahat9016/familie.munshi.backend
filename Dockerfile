################ Build Stage ################
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache Maven dependencies
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN ./mvnw dependency:go-offline -B -q

# Copy source code and build
COPY src src
COPY eclipse-formatter-profile.xml eclipse.importorder license-header ./
RUN ./mvnw clean package -DskipTests -B -q \
    && cp target/familiemunshi-backend-*-SNAPSHOT.jar /build/app.jar

# Extract Spring Boot layers for fast Docker caching
RUN java -Djarmode=tools -jar /build/app.jar extract --destination /build/extracted

################ Runtime Stage ################
FROM eclipse-temurin:21-jre-alpine

# Dedicated non-root user for familiemunshi
RUN addgroup --system --gid 1001 familiemunshi \
    && adduser --system --uid 1001 --ingroup familiemunshi --shell /sbin/nologin familiemunshi

WORKDIR /app

# Copy extracted layers
COPY --from=builder --chown=familiemunshi:familiemunshi /build/extracted/dependencies/ ./
COPY --from=builder --chown=familiemunshi:familiemunshi /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=familiemunshi:familiemunshi /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=familiemunshi:familiemunshi /build/extracted/application/ ./

USER 1001

ENV SPRING_PROFILES_ACTIVE=cloud \
    PORT=10000 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -XX:+UseG1GC"

EXPOSE 10000

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]