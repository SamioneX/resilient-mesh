FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . /app
RUN mvn -q -pl services/transaction-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/services/transaction-service/target/*.jar app.jar
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=70", "-jar", "/opt/app/app.jar"]
