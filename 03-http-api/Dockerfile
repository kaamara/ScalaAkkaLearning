# syntax=docker/dockerfile:1

# ---------- etap 1: build ----------
# Obraz z gotowym sbt i Java 17.
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_2.13.13 AS build

# Czytelniejszy log builda.
ENV SBT_OPTS="-Dsbt.ci=true -Dsbt.color=false -Dsbt.supershell=false"

WORKDIR /build

# Najpierw pliki builda. Zaleznosci zostaja w cache, gdy zmieni sie kod.
COPY build.sbt .
COPY project/build.properties project/
COPY project/plugins.sbt project/
RUN sbt -batch update

# Potem kod. Testy musza przejsc, zeby obraz powstal.
COPY src ./src
RUN sbt -batch test assembly

# ---------- etap 2: runtime ----------
# Samo JRE, bez sbt i kodu zrodlowego. Duzo mniejszy obraz.
FROM eclipse-temurin:17-jre-jammy AS runtime

LABEL org.opencontainers.image.title="scala-akka-http-api" \
      org.opencontainers.image.description="REST na Akka HTTP z /health, /ready i /metrics" \
      org.opencontainers.image.source="https://github.com/kaamara/ScalaAkkaLearning"

# curl tylko po to, zeby dzialal HEALTHCHECK nizej.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

# Aplikacja nie dziala jako root.
RUN groupadd --system --gid 10001 app \
 && useradd --system --uid 10001 --gid app --no-create-home app \
 && mkdir -p /app \
 && chown app:app /app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/scala-2.13/app.jar /app/app.jar

USER 10001:10001

# Port w jednym miejscu: uzywaja go EXPOSE, HEALTHCHECK i aplikacja.
ARG HTTP_PORT=8080

# Main.scala czyta te dwie zmienne. Port zmienia sie bez budowania obrazu:
#   docker run -e HTTP_PORT=9090 ...
ENV HTTP_HOST=0.0.0.0 \
    HTTP_PORT=${HTTP_PORT}

# Bez tego Java psuje polskie znaki w logach i odpowiedziach.
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=UTC

# JAVA_BASE_OPTS: ustawienia stale, m.in. limit pamieci z kontenera.
# JAVA_OPTS: puste, miejsce na wlasne flagi:
#   docker run -e JAVA_OPTS="-Xmx256m" ...
# exit-jvm jest tutaj, a nie w application.conf, bo tamten plik dziala
# takze w testach i ubijalby sbt w polowie.
ENV JAVA_BASE_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dakka.coordinated-shutdown.exit-jvm=on" \
    JAVA_OPTS=""

EXPOSE ${HTTP_PORT}

# Docker co 30 sekund sprawdza, czy /health odpowiada.
# 127.0.0.1 zamiast localhost, bo serwer slucha tylko po IPv4.
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD curl -fsS "http://127.0.0.1:${HTTP_PORT}/health" || exit 1

# sh -c, zeby zmienne JAVA_* sie rozwinely.
# exec, zeby java dostala SIGTERM i zdazyla sie ladnie zamknac.
# "--" pozwala dopisac wlasne argumenty za nazwa obrazu.
ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_BASE_OPTS $JAVA_OPTS -jar /app/app.jar \"$@\"", "--"]
