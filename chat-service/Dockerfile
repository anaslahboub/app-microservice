# Build stage
FROM openjdk:17-jdk-slim AS build

WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the jar file from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]