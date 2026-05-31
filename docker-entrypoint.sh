#!/usr/bin/env bash
# Ensure the brain areas exist on the freshly mounted /brain, then start Claude.
# Runs at container start (AFTER the bind mount is in place) — unlike a Dockerfile
# RUN, which would be shadowed by the mount.
set -euo pipefail

# Areas to ensure on startup, as space-separated "name:sensitivity" pairs.
# Override for an isolated host, e.g. -e BRAIN_AREAS="work:confidential"
BRAIN_AREAS="${BRAIN_AREAS:-profile:personal work:confidential live:personal}"

for pair in $BRAIN_AREAS; do
  name="${pair%%:*}"
  sensitivity="${pair##*:}"
  dir="/brain/${name}"
  if [ ! -f "${dir}/.brain.yml" ]; then
    echo "bootstrapping area: ${name} (${sensitivity})"
    brain init "${dir}" --area "${name}" --sensitivity "${sensitivity}"
  fi
done

exec claude "$@"
