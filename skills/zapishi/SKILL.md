---
name: zapishi
description: Use when the user says «запиши» or «сохрани» — summarize the dialogue and file it as a note in the correct brain area with valid frontmatter and an updated index.
---

# запиши

When the user says «запиши»/«сохрани»:

1. Decide the target domain/area under `/brain` (`work`, `live`, `profile`, …).
   If ambiguous, ask — do not guess.
2. Read the area manifest to get `layout` and `sensitivity`:
   `cat /brain/<area>/.brain.yml`
3. PRIVACY BOUNDARY: if the material draws on a more protected area than the
   target, do not merge it. Warn the user instead.
4. Write the note following `/opt/brain/conventions/note-format.md`:
   filename `YYYY-MM-DD_topic.md`, full frontmatter including rich `keywords`
   (synonyms + natural phrases the user would later search by).
5. Rebuild the index: `brain index /brain/<area>`
6. Confirm what was written and where.
