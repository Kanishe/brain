#!/usr/bin/env bash
# SessionStart: если в vault ещё не подключена ни одна работа (нет /brain/work/*/work.md),
# просит Claude запустить скилл setup-work. Сам ничего не настраивает.

vault="${BRAIN_VAULT:-/brain}"

for f in "$vault"/work/*/work.md; do
  [ -f "$f" ] && exit 0
done

cat <<'EOF'
{"hookSpecificOutput":{"hookEventName":"SessionStart","additionalContext":"В brain не подключена ни одна работа: нет файлов /brain/work/*/work.md. До любой другой работы предложи пользователю запустить скилл setup-work (подключение Jira и Confluence MCP для работы). Если пользователь откажется, не настаивай."}}
EOF
