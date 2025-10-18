FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create the uploads directory and set permissions
RUN mkdir -p /app/chat-uploads && chmod 755 /app/chat-uploads

COPY --from=build /app/target/chat-service*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]