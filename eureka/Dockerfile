
FROM maven:3-eclipse-temurin-17 AS builder
WORKDIR /app

COPY . .

RUN mvn -N install

RUN mvn dependency:go-offline -DskipTests

RUN mvn clean package -pl eureka -am -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/eureka/target/*.jar app.jar
EXPOSE 8761
CMD ["java", "-jar", "app.jar"]
