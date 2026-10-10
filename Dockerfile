# =========================
# Stage 1: Build
# =========================
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy Maven files first
COPY pom.xml .

# Download dependencies
RUN apt-get update \
    && apt-get install -y maven \
    && mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build Spring Boot application
RUN mvn clean package -DskipTests


# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the generated JAR
COPY --from=build /app/target/first-project-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]