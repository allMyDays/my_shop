FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/gateway-api-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8087 87

ENTRYPOINT ["java", "-jar", "app.jar"]