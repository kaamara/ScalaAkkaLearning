#!/usr/bin/env bash
set -e

# Uruchamiamy kontener i sprawdzamy realne endpointy HTTP. Kontener sprząta trap na końcu.
docker run -d --name api -p 8080:8080 "$IMAGE" > /dev/null
trap 'docker rm -f api > /dev/null 2>&1 || true' EXIT
curl -fsS --retry 10 --retry-all-errors http://localhost:8080/health | grep -q 'UP'
curl -fsS -X POST http://localhost:8080/counter/increment | grep -q '"count":1'
curl -fsS http://localhost:8080/metrics | grep -qx 'app_counter_value 1'
