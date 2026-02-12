# CI stage: use --target=prebuilt with pre-built JAR in build context
FROM eclipse-temurin:25-jre AS prebuilt
WORKDIR /app
COPY webserver/build/libs/habit-evaluator-web.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Local development: full multi-stage build (default)
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle/ gradle/
COPY shared/ shared/
COPY webserver/ webserver/
RUN ./gradlew :webserver:bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/webserver/build/libs/habit-evaluator-web.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
