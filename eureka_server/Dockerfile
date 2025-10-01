FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/eureka_server-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]