FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_2.13.13 AS build
WORKDIR /build
COPY build.sbt .
COPY project/build.properties project/plugins.sbt project/
RUN sbt -batch update
COPY src src
RUN sbt -batch test assembly

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /build/target/scala-2.13/app.jar /app.jar
USER 1000
ENTRYPOINT ["java", "-jar", "/app.jar"]
