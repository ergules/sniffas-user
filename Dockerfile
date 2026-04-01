# Stage 1: Build
FROM maven:3.8.6-amazoncorretto-11 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline   # downloads & caches deps as its own layer
COPY src ./src
RUN mvn clean package -DskipTests  # only re-runs if src changes

# Stage 2: Run
FROM amazoncorretto:11-alpine3.15
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]