FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/gitway-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 7777

ENTRYPOINT ["java", "-jar", "app.jar"]
