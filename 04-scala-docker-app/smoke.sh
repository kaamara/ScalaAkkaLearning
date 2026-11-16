#!/usr/bin/env bash
set -e

export COMPOSE_PROJECT_NAME=smoke-04
export POSTGRES_PASSWORD="$(openssl rand -hex 16)"
export APP_IMAGE="$IMAGE"
trap 'rc=$?; [ "$rc" -ne 0 ] && docker compose logs; docker compose down -v > /dev/null 2>&1 || true; exit $rc' EXIT
docker compose up -d --no-build --wait
curl -fsS --retry 5 --retry-all-errors http://localhost:8080/ | grep -q 'Panel Sterowania'
curl -fsS http://localhost:8080/api/ | grep -q 'Backend gotowy'
curl -fsS -X POST --data 'smoke-test-ci' http://localhost:8080/api/add
curl -fsS http://localhost:8080/api/messages | grep -q 'smoke-test-ci'
[[ "$(curl -s -o /dev/null -w '%{http_code}' -X POST --data '' http://localhost:8080/api/add)" == 400 ]]
