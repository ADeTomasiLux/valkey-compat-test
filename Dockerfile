FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress package -DskipTests

FROM eclipse-temurin:11-jre
RUN useradd --system --uid 10001 app
COPY --from=build /src/target/valkey-compat-test-1.0.0.jar /app.jar
USER 10001
ENTRYPOINT ["java", "-jar", "/app.jar"]
