#!/usr/bin/env sh
set -eu

LIMIT="${KOTLIN_LINE_LIMIT:-100}"
FAILED=0

find app/src/main -type f -name '*.kt' | sort | while IFS= read -r file; do
  lines="$(wc -l < "$file" | tr -d ' ')"
  if [ "$lines" -gt "$LIMIT" ]; then
    printf '%s %s\n' "$lines" "$file"
    FAILED=1
  fi
done > /tmp/hyperpos-kotlin-line-audit.txt

cat /tmp/hyperpos-kotlin-line-audit.txt

if [ -s /tmp/hyperpos-kotlin-line-audit.txt ]; then
  printf 'Kotlin production line audit failed. Limit: %s lines.\n' "$LIMIT" >&2
  exit 1
fi

printf 'Kotlin production line audit passed. Limit: %s lines.\n' "$LIMIT"
