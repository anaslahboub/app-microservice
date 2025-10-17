FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src/main/resources src/main/resources

RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/target/discovery-service*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]