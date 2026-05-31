# portable-brain

A portable Claude Code container that carries rules, not data. Your knowledge
lives on the host mount `/brain` and you sync it yourself (git/cloud).

## Run

    docker run -it -v ~/brain:/brain portable-brain

Isolate a single domain (e.g. on a work machine):

    docker run -it -v ~/brain-work:/brain/work portable-brain

## Auth (Claude Max/Pro subscription)

The agent's rules (`CLAUDE.md`) and skills are baked into the image under
`/root/.claude/`, where Claude Code discovers them. **Do not bind-mount over
`/root/.claude`** for auth — it would shadow those baked rules and skills.

Recommended: authenticate with a subscription token (generate once on a machine
with a browser via `claude setup-token`) and pass it in:

    docker run -it -v ~/brain:/brain \
      -e CLAUDE_CODE_OAUTH_TOKEN=<token> portable-brain

Do **not** set `ANTHROPIC_API_KEY` if you want subscription billing. To persist
an interactive `/login` instead, mount only the credentials file, not the whole
dir: `-v ~/brain-claude/.credentials.json:/root/.claude/.credentials.json`.

## Layout

    /brain/profile   who you are / how to work with you (live facts)
    /brain/work      work, sensitivity: confidential
    /brain/live      personal

Each area has a `.brain.yml` manifest and a self-healing `MOC.md` index.

## Startup bootstrap

On container start (after `/brain` is mounted) the entrypoint ensures the brain
areas exist, creating any that are missing via `brain init` — manifest + index,
not just empty folders. It is idempotent: existing areas are left untouched.

> A Dockerfile `RUN mkdir` would not work here — the `/brain` bind mount shadows
> anything the image created at build time, so areas must be created at runtime.

Default areas: `profile:personal`, `work:confidential`, `live:personal`.
Override with the `BRAIN_AREAS` env var (space-separated `name:sensitivity`
pairs) — useful for an isolated host:

    docker run -it -v ~/brain-work:/brain/work \
      -e BRAIN_AREAS="work:confidential" portable-brain

## Helper tools (the `brain` CLI)

    brain init <path> --area NAME --sensitivity LEVEL
    brain index <area_dir>
    brain recall <brain_root> "<phrase>" [--into LEVEL] [--max-results N]
    brain validate <file>     # a .brain.yml manifest or a note .md

## Development

    npm install
    npm test        # node --test
    bash test/e2e.sh
