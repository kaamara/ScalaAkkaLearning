FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_2.13.13 AS builder
WORKDIR /app
COPY project/plugins.sbt project/build.properties ./project/
COPY build.sbt .
RUN sbt update
COPY . .
RUN sbt assembly
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/scala-2.13/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]