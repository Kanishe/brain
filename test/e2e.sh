#!/usr/bin/env bash
# End-to-end check of the helper tools against a temp brain, no Claude/Docker needed.
set -euo pipefail
cd "$(dirname "$0")/.."
CLI="node brain_tools/cli.js"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

$CLI init "$TMP/work" --area work --sensitivity confidential
$CLI init "$TMP/live" --area live --sensitivity personal

cat > "$TMP/work/2026-05-31_auth-service.md" <<'EOF'
---
schema_version: 1
date: 2026-05-31
tags: [java, auth]
keywords: [авторизация, jwt, сервис авторизации]
related: []
summary: JWT auth service for Acme
---
# Auth
EOF

$CLI index "$TMP/work"
grep -q "auth-service" "$TMP/work/MOC.md"

# recall finds the work note
$CLI recall "$TMP" "сервис авторизации" | grep -q "auth-service"

# privacy boundary: confidential work note hidden when filing into personal
if $CLI recall "$TMP" "авторизация" --into personal | grep -q "auth-service"; then
  echo "PRIVACY BOUNDARY FAILED" >&2
  exit 1
fi

echo "E2E OK"
