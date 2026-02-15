# Use Maven + JDK image to build the app
FROM maven:3.9.0-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# Use lightweight JDK image for running
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built JAR from previous stage
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 9090

# Run
ENTRYPOINT ["java","-jar","app.jar"]
