FROM eclipse-temurin:11-jdk AS builder
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y curl
RUN curl -fL https://github.com/sbt/sbt/releases/download/v1.9.7/sbt-1.9.7.tgz | tar xz
RUN ./sbt/bin/sbt assembly

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /app/target/scala-2.13/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
