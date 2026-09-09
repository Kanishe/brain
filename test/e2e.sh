#!/usr/bin/env bash
# End-to-end check of the brain CLI against a temp brain, no Claude/Docker needed.
set -euo pipefail
cd "$(dirname "$0")/.."

# Build the jar if missing, then drive the CLI through the same wrapper the image uses.
if [ ! -f target/brain-tools.jar ]; then
  mvn -B -q -DskipTests package
fi
export BRAIN_JAR="$PWD/target/brain-tools.jar"
CLI="$PWD/bin/brain"

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

"$CLI" init "$TMP/work" --area work --sensitivity confidential
"$CLI" init "$TMP/live" --area live --sensitivity personal

# The body uses `---` horizontal rules, as note-format.md requires: only the
# frontmatter is YAML, and the CLI must not read the rest as a document stream.
cat > "$TMP/work/2026-05-31_auth-service.md" <<'EOF'
---
schema_version: 1
date: 2026-05-31
tags: [java, auth]
keywords: [авторизация, jwt, сервис авторизации]
related: []
summary: JWT auth service for Acme
---

# 🔐 Auth

> **💡 TL;DR** — JWT auth service.

---

## 📖 Разбор

body

---

## 📌 Что запомнить

1. token
EOF

"$CLI" index "$TMP/work"
grep -q "auth-service" "$TMP/work/MOC.md"

# recall finds the work note (Cyrillic query)
"$CLI" recall "$TMP" "сервис авторизации" | grep -q "auth-service"

# privacy boundary: confidential work note hidden when filing into personal
if "$CLI" recall "$TMP" "авторизация" --into personal | grep -q "auth-service"; then
  echo "PRIVACY BOUNDARY FAILED" >&2
  exit 1
fi

# validate accepts a well-formed manifest and a well-formed note
"$CLI" validate "$TMP/work/.brain.yml"
"$CLI" validate "$TMP/work/2026-05-31_auth-service.md"

# validate reports schema errors on a note, rather than crashing on its body
cat > "$TMP/work/2026-06-01_broken.md" <<'EOF'
---
schema_version: 1
date: 2026-06-01
---

# Broken

---

frontmatter has no summary
EOF

if out="$("$CLI" validate "$TMP/work/2026-06-01_broken.md" 2>&1)"; then
  echo "VALIDATE ACCEPTED A NOTE WITHOUT summary" >&2
  exit 1
fi
grep -q "summary is required" <<<"$out"

# validate refuses a directory with a message instead of a stack trace
if out="$("$CLI" validate "$TMP/work" 2>&1)"; then
  echo "VALIDATE ACCEPTED A DIRECTORY" >&2
  exit 1
fi
grep -q "is a directory" <<<"$out"

echo "E2E OK"
