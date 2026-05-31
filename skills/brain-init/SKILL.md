---
name: brain-init
description: Use when a mounted area has no manifest, or the user wants to start a new area/domain — scaffold a valid .brain.yml and index.
---

# brain-init

When an area is missing its `.brain.yml`, or the user starts a new area:

1. Confirm the area name and sensitivity
   (`public` | `personal` | `confidential`).
2. Scaffold it:
   `brain init /brain/<area> --area <name> --sensitivity <level>`
3. Show the created manifest and confirm the area is ready.
