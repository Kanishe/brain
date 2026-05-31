# Convention: area manifest (.brain.yml)

Every area has a `.brain.yml` in its root. It is the self-describing map of
that area and travels with the data.

Fields:
- `schema_version` (int, required) — currently 1.
- `area` (string, required) — short name, e.g. `work`, `live`, `profile`.
- `description` (string) — what lives here.
- `layout` (string) — naming pattern for new notes, e.g. `<company>/<topic>`.
- `index` (string) — index filename, default `MOC.md`.
- `sensitivity` (`public` | `personal` | `confidential`) — privacy level.
- `keywords` (list) — terms that route recall to this area.

Validate a manifest:

    brain validate /brain/work/.brain.yml

Create a new area (optionally seed description/layout/keywords in one command):

    brain init /brain/work --area work --sensitivity confidential
    brain init /brain/education --area education --sensitivity personal \
      --description "Обучение" --layout "<it|english>/<topic>" \
      --keywords "обучение, it, english"

`--keywords` is a comma-separated list.
