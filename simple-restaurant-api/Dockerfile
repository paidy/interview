FROM gradle:jdk11-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:11
COPY --from=build /home/gradle/src/build/libs/*.jar /app/
EXPOSE 8080
CMD java -jar /app/simplerestaurantapi-0.0.1-SNAPSHOT.jar
