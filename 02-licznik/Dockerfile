# ---------- etap 1: build ----------
# Gotowy obraz sbt zamiast recznego curl na tarball w kazdym buildzie.
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_2.13.13 AS build
WORKDIR /build

# Najpierw same pliki definicji builda. Warstwa z pobranymi zaleznosciami
# przezywa wtedy kazda zmiane w kodzie zrodlowym — wczesniejsze "COPY . ."
# uniewazmialo cache przy poprawce jednej linii.
COPY project/build.properties project/plugins.sbt ./project/
COPY build.sbt .
RUN sbt -batch update

COPY src ./src
# Testy w buildzie obrazu: nie da sie zbudowac obrazu z czerwonych testow.
RUN sbt -batch test assembly

# ---------- etap 2: runtime ----------
FROM eclipse-temurin:17-jre-jammy AS runtime

# Uzytkownik bez uprawnien roota. Kubernetes z PodSecurityStandards
# odrzuca obrazy dzialajace jako root.
RUN groupadd --system --gid 10001 app \
 && useradd --system --uid 10001 --gid app --no-create-home app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/scala-2.13/app.jar /app/app.jar
USER 10001:10001

# Bez tego JVM w kontenerze liczy heap od pamieci hosta, nie od limitu cgroup.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC"

# Aplikacja konsolowa — czyta stdin, wiec uruchamiaj ja z docker run -it.
ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
