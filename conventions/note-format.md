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

## Body formatting

The frontmatter and filename are machine-read — never change their structure.
The body below follows these rules (see `templates/note.md` for a live example):

- **Title** starts with one topical emoji: `# ⚡ Build time и runtime в Quarkus`.
- **TL;DR block** right under the title, always present:
  `> **💡 TL;DR** — суть в 1–2 предложениях.`
- **Section emoji markers** — emoji are navigation markers for headings and
  callouts only, not decoration inside prose. Standard set:
  - 🎯 `## 🎯 Главная идея` — the core thesis
  - 📖 `## 📖 Разбор` — main content (free-form subsections inside)
  - 📌 `## 📌 Что запомнить` — numbered takeaways, closes every note
  - 🔗 `## 🔗 Связанное` — wikilinks with a short "why related"
- **Callouts** are blockquotes with a bold emoji label:
  - `> ⚠️ **Грабли:** …` — pitfalls and warnings
  - `> 📝 **Примечание:** …` — easy-to-miss clarifications
- **Horizontal rules** (`---`) separate major blocks: after the TL;DR and
  before «Что запомнить».
- **Tables**: align columns with `:---` / `:---:`; use ✅ / ❌ for yes/no cells.
- **Diagrams**: fenced code blocks with Unicode box-drawing
  (`┌ ─ ┐ │ └ ┘ ▶ ▼`), not bare ASCII art (`+--|`).

After writing or editing a note, rebuild the area index:

    brain index /brain/<area>
