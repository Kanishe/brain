# Convention: index (MOC.md) and self-healing

Each area has an index `MOC.md` listing its notes (`- [[name]] — summary`).

The index is derived data: it is always rebuilt from note frontmatter, never
hand-maintained. If it is missing or stale after a manual file move, rebuild it:

    brain index /brain/<area>

Recovery order, each level rebuildable from the one below:
`.brain.yml` (manifest) -> `MOC.md` (index) -> note frontmatter.

## Scope: areas only, never the brain root

`brain index <dir>` walks the *entire* subtree of `<dir>` and flattens every
note it finds into one `MOC.md` — it has no concept of area boundaries or
`sensitivity`. Only ever run it on an area directory (one that has its own
`.brain.yml`), never on the `/brain` root itself: the root has no manifest
and spans every area, so indexing it would pull notes from `confidential`
and `personal` areas straight into an unbounded index. The CLI refuses to
index a directory without a `.brain.yml` for this reason.

The root `/brain/MOC.md` is the one exception to "index is derived, never
hand-maintained" — it stays hand-curated (a short list of links into each
area's own `MOC.md`), precisely because it can't be safely auto-generated.
