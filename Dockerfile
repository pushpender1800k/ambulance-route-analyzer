# ═══════════════════════════════════════════════════
# RESQ — Multi-stage Production Dockerfile
# ═══════════════════════════════════════════════════

# ── Stage 1: Build ──
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -Pprod

# ── Stage 2: Run ──
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S resq && adduser -S resq -G resq

# Copy the exact JAR (pom.xml finalName=app)
COPY --from=builder /app/target/app.jar app.jar

RUN mkdir -p /var/log/resq && chown resq:resq /var/log/resq
USER resq

ENV PORT=8080

ENTRYPOINT exec java \
  -Xms256m -Xmx512m \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod \
  -Dserver.port=$PORT \
  -jar app.jar
