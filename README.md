# portable-brain

A portable Claude Code container that carries rules, not data. Your knowledge
lives on the host mount `/brain` and you sync it yourself (git/cloud).

> 🇷🇺 Описание на русском — [README.ru.md](README.ru.md)

## Run

    docker run -it -v ~/brain:/brain portable-brain

Isolate a single domain (e.g. on a work machine):

    docker run -it -v ~/brain-work:/brain/work portable-brain

## Docker Compose

A `docker-compose.yml` is included (mounts `~/brain`, keeps an interactive TTY):

    docker compose build           # build the image
    docker compose run --rm brain  # start the agent (interactive Claude TUI)

Use `run --rm` (not `up`) so the Claude Code terminal attaches to your shell.
Login/areas/persistence options are documented inline in the compose file.

## Auth (Claude Max/Pro subscription)

The agent's rules (`CLAUDE.md`) and skills are baked into the image under
`/root/.claude/`, where Claude Code discovers them. **Do not bind-mount over
`/root/.claude`** for auth — it would shadow those baked rules and skills. Also
do **not** set `ANTHROPIC_API_KEY` if you want subscription billing.

Primary: interactive login with your Max/Pro subscription. Just run the
container and inside Claude type `/login` → *Log in with Claude account* →
approve in the browser. No token needed.

To keep the login across runs, mount only the credentials file (not the whole
dir, which would shadow the baked rules):

    touch ~/brain-creds.json
    docker run -it -v ~/brain:/brain \
      -v ~/brain-creds.json:/root/.claude/.credentials.json portable-brain

Headless option (servers without a browser): generate a subscription token once
with `claude setup-token` (it bills to your subscription, it is **not** an API
key) and pass it as `-e CLAUDE_CODE_OAUTH_TOKEN=<token>`.

## Layout

    /brain/profile     who you are / how to work with you (live facts)
    /brain/work        work, sensitivity: confidential
    /brain/live        personal
    /brain/education   learning; sub-folders: it (tech), english (language)

Each area has a `.brain.yml` manifest and a self-healing `MOC.md` index.
Learning/study topics are routed to `education/it` or `education/english`
(see the routing rule in `CLAUDE.md`).

## Startup bootstrap

On container start (after `/brain` is mounted) the entrypoint ensures the brain
areas exist, creating any that are missing via `brain init` — manifest + index,
not just empty folders. It is idempotent: existing areas are left untouched.

> A Dockerfile `RUN mkdir` would not work here — the `/brain` bind mount shadows
> anything the image created at build time, so areas must be created at runtime.

Default areas: `profile:personal`, `work:confidential`, `live:personal`,
`education:personal`.
Override with the `BRAIN_AREAS` env var (space-separated `name:sensitivity`
pairs) — useful for an isolated host:

    docker run -it -v ~/brain-work:/brain/work \
      -e BRAIN_AREAS="work:confidential" portable-brain

## Helper tools (the `brain` CLI)

    brain init <path> --area NAME --sensitivity LEVEL \
      [--description TEXT] [--layout PATTERN] [--keywords "a, b, c"]
    brain index <area_dir>
    brain recall <brain_root> "<phrase>" [--into LEVEL] [--max-results N]
    brain validate <file>     # a .brain.yml manifest or a note .md

## Development

The `brain` CLI is a Java 21 app (built to a fat jar); the agent runtime
(Claude Code) is Node, so the image is polyglot (Node + a JRE).

    mvn test                       # JUnit (JDK 21)
    mvn -q -DskipTests package     # build target/brain-tools.jar
    bash test/e2e.sh               # builds the jar if needed, drives the brain CLI

The `brain` wrapper (`bin/brain`) sets a UTF-8 locale before launching the jar
so non-ASCII args/output (e.g. Cyrillic) round-trip correctly.
