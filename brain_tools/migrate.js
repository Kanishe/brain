// Migrate note frontmatter to the current schema version.

import { CURRENT_SCHEMA_VERSION } from "./schema.js";

export function needsMigration(meta) {
  return meta.schema_version !== CURRENT_SCHEMA_VERSION;
}

// Return a new metadata object upgraded to the current schema version.
export function migrateNote(meta) {
  const out = { ...meta };
  if (out.keywords === undefined) out.keywords = [];
  out.schema_version = CURRENT_SCHEMA_VERSION;
  return out;
}
