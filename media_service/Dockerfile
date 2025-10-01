FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/media_service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8078 78

ENTRYPOINT ["java", "-jar", "app.jar"]