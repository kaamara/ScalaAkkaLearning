# syntax=docker/dockerfile:1

# ---------- etap 1: build ----------
# Obraz z gotowym sbt i Java 17.
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_2.13.13 AS build

# Czytelniejszy log builda.
ENV SBT_OPTS="-Dsbt.ci=true -Dsbt.color=false -Dsbt.supershell=false"

WORKDIR /build

# Najpierw pliki builda. Zaleznosci zostaja w cache, gdy zmieni sie kod.
COPY project/build.properties project/plugins.sbt ./project/
COPY build.sbt .
RUN sbt -batch update

# Potem kod. Testy musza przejsc, zeby obraz powstal.
COPY src ./src
RUN sbt -batch test assembly

# ---------- etap 2: runtime ----------
# Samo JRE na alpine. Najmniejszy z obrazow w tym repo.
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL org.opencontainers.image.title="scala-docker-app" \
      org.opencontainers.image.description="Backend Cask + PostgreSQL za proxy nginx" \
      org.opencontainers.image.source="https://github.com/kaamara/ScalaAkkaLearning"

# Aplikacja nie dziala jako root. Na alpine sluza do tego addgroup/adduser.
RUN addgroup -S -g 10001 app \
 && adduser  -S -u 10001 -G app -H app \
 && mkdir -p /app \
 && chown app:app /app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/scala-2.13/app.jar /app/app.jar
USER 10001:10001

# Dane do bazy podaje docker-compose.yml: DB_URL, DB_USER, DB_PASSWORD.
# Celowo nie ma ich tutaj - haslo nie powinno siedziec w obrazie.
# Bez nich aplikacja nie wstanie i od razu o tym powie.

# Port w jednym miejscu: uzywaja go EXPOSE, HEALTHCHECK i aplikacja.
ARG HTTP_PORT=8081

# Main.scala czyta te dwie zmienne. Port zmienia sie bez budowania obrazu:
#   docker run -e HTTP_PORT=9090 ...
ENV HTTP_HOST=0.0.0.0 \
    HTTP_PORT=${HTTP_PORT}

# Bez tego Java psuje polskie znaki w wiadomosciach.
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=UTC

# JAVA_BASE_OPTS: ustawienia stale, m.in. limit pamieci z kontenera.
# JAVA_OPTS: puste, miejsce na wlasne flagi:
#   docker run -e JAVA_OPTS="-Xmx256m" ...
ENV JAVA_BASE_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8" \
    JAVA_OPTS=""

EXPOSE ${HTTP_PORT}

# Docker co 30 sekund sprawdza, czy aplikacja odpowiada.
# wget jest juz w alpine, curl nie jest potrzebny.
# 127.0.0.1 zamiast localhost, bo serwer slucha tylko po IPv4.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider "http://127.0.0.1:${HTTP_PORT}/" || exit 1

# sh -c, zeby zmienne JAVA_* sie rozwinely.
# exec, zeby java dostala SIGTERM i zdazyla zamknac polaczenia z baza.
# "--" pozwala dopisac wlasne argumenty za nazwa obrazu.
ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_BASE_OPTS $JAVA_OPTS -jar /app/app.jar \"$@\"", "--"]
