FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/cart-wishlist_service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080 80

ENTRYPOINT ["java", "-jar", "app.jar"]