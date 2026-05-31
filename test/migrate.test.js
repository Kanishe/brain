import test from "node:test";
import assert from "node:assert/strict";
import * as migrate from "../brain_tools/migrate.js";
import { CURRENT_SCHEMA_VERSION } from "../brain_tools/schema.js";

test("migrate sets current version", () => {
  const out = migrate.migrateNote({ date: "2026-05-31", summary: "x" });
  assert.equal(out.schema_version, CURRENT_SCHEMA_VERSION);
});

test("migrate adds missing keywords", () => {
  const out = migrate.migrateNote({ schema_version: 0, date: "2026-05-31", summary: "x" });
  assert.deepEqual(out.keywords, []);
});

test("migrate preserves existing fields", () => {
  const out = migrate.migrateNote({
    schema_version: 0,
    date: "2026-05-31",
    summary: "x",
    keywords: ["auth"],
    tags: ["java"],
  });
  assert.deepEqual(out.keywords, ["auth"]);
  assert.deepEqual(out.tags, ["java"]);
});

test("needsMigration detects old version", () => {
  assert.equal(migrate.needsMigration({ schema_version: 0 }), true);
  assert.equal(migrate.needsMigration({ schema_version: 1 }), false);
});
