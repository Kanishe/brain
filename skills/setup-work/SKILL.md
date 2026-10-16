---
name: setup-work
description: Подключить новую работу (организацию) в brain - Jira и Confluence MCP, токены через env, папка work/<работа>/ с work.md и .mcp.json. Используй после клона brain, при смене работы или когда пользователь просит подключить Jira/Confluence для новой компании.
---

# Подключение работы

Работа = организация со своими Jira и Confluence. Настраивается один раз, проекты внутри неё (скилл `new-project`) наследуют подключение.

Структура:

```
work/<работа>/
  work.md       # URL, имена env-переменных, Jira-проект по умолчанию
  .mcp.json     # jira + confluence, токены через ${ENV}, можно коммитить
  <проект>/project.md
```

## Правила

- Токены никогда не пишутся в файлы brain и не повторяются в чате. Только имена env-переменных.
- Задавай вопросы по одному.
- Не пиши длинное тире, только обычный "-".

## Шаги

### 0. Название работы

Спроси: "Как называется работа (организация)?" Преобразуй в slug: латиница, нижний регистр, `_` вместо пробелов (`СберДевайсы` -> `sberdevices`). Покажи slug и подтверди.

Если `work/<slug>/work.md` уже есть - покажи его и спроси, перенастроить или выйти.

### 1. Адреса MCP и токены

Спроси URL MCP-серверов Jira и Confluence. Для `sberdevices` предложи известные:
- jira: `https://jira.mcp.gigab2b.ai/mcp`
- confluence: `https://confluence.mcp.gigab2b.ai/mcp`
- схема заголовка: `Authorization: Token <токен>`

Также спроси: адрес самой Jira в браузере (для ссылок) и Jira-проект по умолчанию (для sberdevices: `https://tasks.sberdevices.ru`, `KPROD`).

Имена переменных: `JIRA_MCP_TOKEN_<SLUG>` и `CONFLUENCE_MCP_TOKEN_<SLUG>` (SLUG в верхнем регистре). Попроси пользователя прописать их в `~/.zshrc` (или `~/.bashrc`) сам, не присылая токены в чат:

```
export JIRA_MCP_TOKEN_<SLUG>=...
export CONFLUENCE_MCP_TOKEN_<SLUG>=...
```

и ответить "готово". Жди ответа.

После ответа проверь, что переменные видны (не печатая значения):

```bash
for v in JIRA_MCP_TOKEN_<SLUG> CONFLUENCE_MCP_TOKEN_<SLUG>; do [ -n "${!v}" ] && echo "$v ok" || echo "$v MISSING"; done
```

Если MISSING - переменная задана, но текущий процесс Claude её не видит. Попроси выполнить `source ~/.zshrc` в терминале и перезапустить `claude` из этой же папки, затем вызвать `/setup-work` снова. Файлы создать можно и сейчас.

### 2. Файлы работы

Создай `work/<slug>/.mcp.json`:

```json
{
  "mcpServers": {
    "jira": {
      "type": "http",
      "url": "<jira mcp url>",
      "headers": { "Authorization": "Token ${JIRA_MCP_TOKEN_<SLUG>}" }
    },
    "confluence": {
      "type": "http",
      "url": "<confluence mcp url>",
      "headers": { "Authorization": "Token ${CONFLUENCE_MCP_TOKEN_<SLUG>}" }
    }
  }
}
```

Создай `work/<slug>/work.md`:

```markdown
---
type: work
work: <slug>
jira_url: <адрес Jira в браузере>
jira_mcp_url: <jira mcp url>
confluence_mcp_url: <confluence mcp url>
jira_token_env: JIRA_MCP_TOKEN_<SLUG>
confluence_token_env: CONFLUENCE_MCP_TOKEN_<SLUG>
default_jira_project: <ключ, например KPROD>
---

# <Название работы>

Подключение Jira и Confluence для работы. Проекты - подпапки, у каждой `project.md`.
```

### 3. Проверка подключения

Подключение проверяется MCP-инструментами `mcp__jira__*` и `mcp__confluence__*` (найди их через ToolSearch по словам "jira", "confluence"):
- Jira: прочитать проект `default_jira_project`.
- Confluence: любой поиск или список пространств.

Если инструментов нет или сервер не подключился - это не ошибка настройки. Объясни: `.mcp.json` работы подхватывается, только когда `claude` запущен из `work/<slug>/` (или её подпапки с копией `.mcp.json`). При первом запуске Claude спросит, доверять ли серверам из `.mcp.json` - ответить да. Проверить: `/mcp`.

### 4. Итог

Кратко: что создано, имена переменных, как проверить (`cd work/<slug> && claude`, затем `/mcp`). Предложи сразу завести первый проект скиллом `new-project`.
