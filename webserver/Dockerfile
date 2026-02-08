FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle/ gradle/
COPY shared/ shared/
COPY webserver/ webserver/
RUN ./gradlew :webserver:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/webserver/build/libs/habit-evaluator-web.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
