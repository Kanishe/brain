#!/usr/bin/env bash
# Ensure the brain areas exist on the freshly mounted /brain, then start Claude.
# Runs at container start (AFTER the bind mount is in place) — unlike a Dockerfile
# RUN, which would be shadowed by the mount.
set -euo pipefail

# Areas to ensure on startup, as space-separated "name:sensitivity" pairs.
# Override for an isolated host, e.g. -e BRAIN_AREAS="work:confidential"
BRAIN_AREAS="${BRAIN_AREAS:-profile:personal work:confidential live:personal education:personal}"

for pair in $BRAIN_AREAS; do
  name="${pair%%:*}"
  sensitivity="${pair##*:}"
  dir="/brain/${name}"
  if [ ! -f "${dir}/.brain.yml" ]; then
    echo "bootstrapping area: ${name} (${sensitivity})"
    brain init "${dir}" --area "${name}" --sensitivity "${sensitivity}"
  fi
done

# ensure superpowers plugin is present (build-time install can silently no-op
# on network failure — see Dockerfile); retry here where runtime network is
# more likely available (non-fatal either way)
if claude plugin list 2>/dev/null | grep -q "superpowers@superpowers-marketplace"; then
  echo "plugin: superpowers enabled"
else
  echo "plugin: superpowers not detected — installing now"
  if claude plugin marketplace add obra/superpowers-marketplace \
      && claude plugin install superpowers@superpowers-marketplace; then
    echo "plugin: superpowers installed"
  else
    echo "plugin: superpowers install failed — continuing without it"
  fi
fi

exec claude "$@"
