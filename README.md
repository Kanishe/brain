# portable-brain

A portable Claude Code container that carries rules, not data. Your knowledge
lives on the host mount `/brain` and you sync it yourself (git/cloud).

## Run

    docker run -it -v ~/brain:/brain portable-brain

Isolate a single domain (e.g. on a work machine):

    docker run -it -v ~/brain-work:/brain/work portable-brain

## Layout

    /brain/profile   who you are / how to work with you (live facts)
    /brain/work      work, sensitivity: confidential
    /brain/live      personal

Each area has a `.brain.yml` manifest and a self-healing `MOC.md` index.

## Helper tools (the `brain` CLI)

    brain init <path> --area NAME --sensitivity LEVEL
    brain index <area_dir>
    brain recall <brain_root> "<phrase>" [--into LEVEL] [--max-results N]
    brain validate <file>     # a .brain.yml manifest or a note .md

## Development

    npm install
    npm test        # node --test
    bash test/e2e.sh
