FROM gradle:8.7.0-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle build -x test

RUN apt-get update && apt-get install -y netcat

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
