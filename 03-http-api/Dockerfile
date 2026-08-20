# syntax=docker/dockerfile:1

# ---------- etap 1: build ----------
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_2.13.13 AS build

WORKDIR /build

# Najpierw same pliki definicji builda. Dzieki temu warstwa z pobranymi
# zaleznosciami przezywa kazda zmiane w kodzie zrodlowym.
COPY build.sbt .
COPY project/build.properties project/
COPY project/plugins.sbt project/
RUN sbt update

COPY src ./src
RUN sbt test assembly

# ---------- etap 2: runtime ----------
FROM eclipse-temurin:17.0.10_7-jre-jammy AS runtime

# curl potrzebny wylacznie do HEALTHCHECK
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

# Uzytkownik bez uprawnien roota. Kubernetes z PodSecurityStandards
# odrzuci obraz dzialajacy jako root.
RUN groupadd --system --gid 10001 app \
 && useradd --system --uid 10001 --gid app --no-create-home app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/scala-2.13/app.jar /app/app.jar

USER 10001:10001

ENV HTTP_HOST=0.0.0.0 \
    HTTP_PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Dakka.coordinated-shutdown.exit-jvm=on"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD curl -fsS http://localhost:8080/health || exit 1

# forma exec + sh -c, zeby JAVA_OPTS bylo rozwiniete
ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
