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
# Samo JRE, bez sbt i kodu zrodlowego. Duzo mniejszy obraz.
FROM eclipse-temurin:17-jre-jammy AS runtime

LABEL org.opencontainers.image.title="scala-akka-kalkulator" \
      org.opencontainers.image.description="Kalkulator na aktorach Akka (aplikacja konsolowa)" \
      org.opencontainers.image.source="https://github.com/kaamara/ScalaAkkaLearning"

# Aplikacja nie dziala jako root.
RUN groupadd --system --gid 10001 app \
 && useradd --system --uid 10001 --gid app --no-create-home app \
 && mkdir -p /app \
 && chown app:app /app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/scala-2.13/app.jar /app/app.jar
USER 10001:10001

# Bez tego Java psuje polskie znaki na wyjsciu.
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=UTC

# JAVA_BASE_OPTS: ustawienia stale, m.in. limit pamieci z kontenera.
# JAVA_OPTS: puste, miejsce na wlasne flagi:
#   docker run -e JAVA_OPTS="-Xmx256m" ...
# Zmiana kolektora smieci idzie przez JAVA_BASE_OPTS - dwa naraz nie dzialaja.
ENV JAVA_BASE_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8" \
    JAVA_OPTS=""

# Aplikacja czyta z klawiatury, wiec uruchamiaj: docker run -it <obraz>
# "--" pozwala dopisac wlasne argumenty za nazwa obrazu.
ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_BASE_OPTS $JAVA_OPTS -jar /app/app.jar \"$@\"", "--"]
