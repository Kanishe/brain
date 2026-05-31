---
name: vspomni
description: Use when the user references past work («на прошлом проекте…», «вспомни», «найди») — retrieve candidate notes by keyword before reading any file in full.
---

# вспомни / найди

When the user references prior knowledge:

1. Run recall over the mounted brain:
   `brain recall /brain "<the user's phrase>"`
   If the user is filing INTO a specific area, pass `--into <sensitivity>` to
   honor the privacy boundary.
2. Present the top 2-3 candidates with their one-line summary. Let the user pick.
3. Only after the user picks, read that file in full and use it.
4. If recall returns nothing, say so plainly and offer to widen the query.
