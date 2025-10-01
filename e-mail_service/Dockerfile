FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/e-mail_service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8081 81

ENTRYPOINT ["java", "-jar", "app.jar"]