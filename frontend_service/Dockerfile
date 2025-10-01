FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/frontend_service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8083 83

ENTRYPOINT ["java", "-jar", "app.jar"]