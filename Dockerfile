
FROM maven:3.9-eclipse-temurin-25 AS backend-build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q clean package -DskipTests


FROM eclipse-temurin:25-jre AS backend

ARG PORT=8080

WORKDIR /app

COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
