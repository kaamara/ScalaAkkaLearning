#!/usr/bin/env bash
set -e

# Aplikacja sama wykonuje Increment/Decrement/Reset i wypisuje wynik.
out=$(mktemp)
docker run --rm "$IMAGE" > "$out"
grep -q 'Aktualny licznik: 2$' "$out"
grep -q 'Po resecie: 0$' "$out"
