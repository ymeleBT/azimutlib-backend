# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src ./src
RUN ./mvnw -B -q -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /data/uploads && chown -R spring:spring /data

COPY --from=build /app/target/*.jar app.jar

# /app is owned by root (files were COPYed before switching user), so the
# app's default ./uploads dir can't be created there under the spring user.
# Point it at /data/uploads instead, which is already chown'd above --
# also the directory to mount a Railway Volume at, so uploads survive redeploys.
ENV UPLOAD_DIR=/data/uploads

USER spring
EXPOSE 8080

# Railway injects $PORT at runtime; application.yml falls back to 8080 locally.
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -jar app.jar"]
