# Convention: index (MOC.md) and self-healing

Each area has an index `MOC.md` listing its notes (`- [[name]] — summary`).

The index is derived data: it is always rebuilt from note frontmatter, never
hand-maintained. If it is missing or stale after a manual file move, rebuild it:

    brain index /brain/<area>

Recovery order, each level rebuildable from the one below:
`.brain.yml` (manifest) -> `MOC.md` (index) -> note frontmatter.
