FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/support_service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8079 79

ENTRYPOINT ["java", "-jar", "app.jar"]