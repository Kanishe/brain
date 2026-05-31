# Convention: note format

Filename: `YYYY-MM-DD_topic-kebab-case.md`.

Frontmatter (see `templates/note.md`):
- `schema_version` (int, required) — currently 1.
- `date` (YYYY-MM-DD, required).
- `tags` (list) — topical tags.
- `keywords` (list) — search terms for recall; include synonyms and the
  natural phrases you would later use to ask for this note.
- `related` (list of `[[wikilinks]]`).
- `summary` (string, required) — one line; this is what recall shows you.

After writing or editing a note, rebuild the area index:

    brain index /brain/work
