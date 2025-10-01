FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/user_service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8077 77

ENTRYPOINT ["java", "-jar", "app.jar"]