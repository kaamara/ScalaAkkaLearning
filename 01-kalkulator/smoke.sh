#!/usr/bin/env bash
set -e

# Aplikacja czyta z stdin: podajemy trzy działania i wyjście.
out=$(mktemp)
printf '10 + 5\n20 * 3\n7 / 0\nexit\n' | docker run -i --rm "$IMAGE" > "$out"
grep -q 'Wynik: 15$' "$out"
grep -q 'Wynik: 60$' "$out"
grep -q 'Blad: dzielenie przez zero$' "$out"
