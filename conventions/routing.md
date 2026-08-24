# Convention: area/topic routing

Routing rules decide which area — and which sub-folder inside it — a new note
lands in, before filing (`запиши`) writes anything. Keep these deterministic;
if a topic doesn't clearly match a rule below, ask the user rather than
guessing.

## Education

Learning / study topics (обучение, учёба) → `/brain/education`, sub-folder by
domain:
- `it` — technology/development learning
- `english` — the English language

Create the sub-folder on first use if missing.

### Python (inside `education/it`)

Python notes do not sit at the top level of `it/` — that level is for
general IT/architecture topics (Quarkus, Hibernate, system design, RabbitMQ,
etc.). Python has its own nested tree:

- `education/it/programming_languages/python/` — clearly Python-related
  material; default landing spot for anything Python that isn't covered by
  the two rules below.
- `education/it/programming_languages/python/core/` — basic/fundamental
  Python syntax and language mechanics (data types, control flow,
  comprehensions, OOP/DI patterns) — not framework-specific.
- `education/it/programming_languages/python/frameworks/<name>/` —
  framework-specific material (existing precedent: `frameworks/fastapi/`).

Indexing is not recursive — after filing or moving a note, rebuild the
specific folder's index directly:

    brain index /brain/education/it/programming_languages/python/core

If a note moved out of the `education/it` top level, also rebuild that level
so the stale entry is dropped from its `MOC.md`:

    brain index /brain/education/it
