---
name: potreniruy
description: Use when the user says «потренируй» and names a topic — build a step-by-step practice plan (what/why/short snippet per step, never a full solution) and file it as a note in /brain/education.
---

# потренируй

When the user says «потренируй» and names one or more topics (e.g.
«потренируй меня по asyncio и pytest»):

1. If the topic is vague or spans too much ground for one session, ask what
   exactly to focus on — do not guess scope.
2. Decide the practice mode:
   - **Syntax/primitives** — core language features, or one library's
     standalone commands/data structures → independent steps, each its own
     small task, is fine.
   - **Framework/library** — e.g. FastAPI, SQLAlchemy, an async framework, or
     several primitives meant to be combined into a real workflow → structure
     the steps as milestones of **one small pet-project** (a mini "prod"-like
     app), not disconnected snippets. Treat every invocation as a fresh
     pet-project scoped to this session's steps — don't assume or extend a
     project from an earlier conversation unless the user says to continue it.
     State up front, before the steps:
     - **What the pet-project is** — one sentence.
     - **Project structure** — the file/directory layout, one line per file
       on what it's responsible for (so the user knows where each step's
       function goes).
     - **Подключение** — the full connection/setup snippet (Redis client,
       DB connection, env vars) — this is boilerplate, not the exercise, so
       show it complete and ready to paste as-is.
3. Break the topic into an ordered list of concrete practice steps (typically
   3-7). For each step give three things:
   - **Что делать** — the concrete small task to build
   - **Зачем** — which concept/method/function/pattern it teaches and why it
     matters
   - **Пример** — a complete, runnable function: signature, a one-line
     comment on what it teaches and why, and a full body with the real
     calls. Not a bare one-liner and not a fragment — the user retypes this
     whole function to practice it.
4. **Never write the business-logic integration that wires the steps'
   functions into one running app** (the `main`/route handlers/call graph
   that ties them together) — that's the user's own exercise. Connection
   setup and project structure are boilerplate, not the exercise, so those
   are shown in full per point 2; each step's function is shown in full per
   point 3; only the glue between them is left for the user to write and
   bring back for review.
5. Present the plan in chat and get confirmation before filing anything.
6. Once confirmed, file it as a note per the `запиши` skill's conventions:
   target `/brain/education` (sub-folder `it` for tech topics, nested further
   per `conventions/routing.md`, e.g. Python goes under
   `it/programming_languages/python/...`), full frontmatter per
   `conventions/note-format.md`, filename
   `YYYY-MM-DD_<topic>-practice-plan.md`. Put the ordered steps under
   `## 📌 Что запомнить`. Right after the TL;DR, add a placeholder section
   the user fills in later so the outcome is recallable:
   ```
   ## 🗂️ Репозиторий и результат

   - **Репозиторий:** _(пока пусто — заполнится, когда появится код)_
   - **Статус:** план
   - **Итоги:** _(пусто)_
   ```
7. Rebuild the index: `brain index /brain/education/it/<sub-folder>`.
8. When the user later comes back with their own code, a repo link, or
   progress for one of these steps: review the code against that step's
   "зачем" (point out gaps, don't rewrite it for them), AND update this same
   note's `## 🗂️ Репозиторий и результат` section in place — fill in the
   repo link, set status to `в процессе`/`завершено`, and add a one-line
   итог per finished step. Rebuild the index afterward.
