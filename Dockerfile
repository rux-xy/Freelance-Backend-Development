# Build stage — use a supported Maven image
FROM maven:3.8.8 AS build

WORKDIR /app

# Copy pom.xml + source
COPY pom.xml .
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","app.jar"]
