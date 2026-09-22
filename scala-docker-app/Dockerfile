# ---------- etap 1: build ----------
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_2.13.13 AS build
WORKDIR /build

# Warstwa zaleznosci osobno od kodu — patrz komentarz w 01-kalkulator.
COPY project/build.properties project/plugins.sbt ./project/
COPY build.sbt .
RUN sbt -batch update

COPY src ./src
RUN sbt -batch test assembly

# ---------- etap 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Alpine: addgroup/adduser zamiast groupadd/useradd.
RUN addgroup -S -g 10001 app \
 && adduser  -S -u 10001 -G app -H app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/scala-2.13/app.jar /app/app.jar
USER 10001:10001

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC"

EXPOSE 8081

# wget jest czescia busyboxa w alpine, nie trzeba dokladac curla.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/ || exit 1

# forma exec + sh -c, zeby JAVA_OPTS zostalo rozwiniete przez powloke
ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
