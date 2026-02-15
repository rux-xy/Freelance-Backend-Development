# Use an official OpenJDK 21 image
FROM eclipse-temurin:21-jdk-jammy
  
  # Set working directory inside the container
WORKDIR /app
  
  # Copy Maven build files
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
  
  # Expose the port your Spring Boot app runs on
EXPOSE 9090
  
  # Run the jar
ENTRYPOINT ["java","-jar","app.jar"]
