import test from "node:test";
import assert from "node:assert/strict";
import * as schema from "../brain_tools/schema.js";

test("current schema version is 1", () => {
  assert.equal(schema.CURRENT_SCHEMA_VERSION, 1);
});

test("sensitivity ordering", () => {
  assert.ok(schema.level("public") < schema.level("personal"));
  assert.ok(schema.level("personal") < schema.level("confidential"));
});

test("canCopy allows equal or lower source", () => {
  assert.equal(schema.canCopy("personal", "confidential"), true);
  assert.equal(schema.canCopy("personal", "personal"), true);
});

test("canCopy blocks higher source into lower target", () => {
  assert.equal(schema.canCopy("confidential", "personal"), false);
});

test("validateManifest accepts minimal valid", () => {
  assert.deepEqual(schema.validateManifest({ schema_version: 1, area: "work" }), []);
});

test("validateManifest reports missing area", () => {
  const errors = schema.validateManifest({ schema_version: 1 });
  assert.ok(errors.some((e) => e.includes("area")));
});

test("validateManifest rejects unknown sensitivity", () => {
  const errors = schema.validateManifest({ schema_version: 1, area: "work", sensitivity: "secret" });
  assert.ok(errors.some((e) => e.includes("sensitivity")));
});

test("validateNote requires date and summary", () => {
  const errors = schema.validateNote({ schema_version: 1 });
  assert.ok(errors.some((e) => e.includes("date")));
  assert.ok(errors.some((e) => e.includes("summary")));
});

test("validateNote accepts valid", () => {
  assert.deepEqual(schema.validateNote({ schema_version: 1, date: "2026-05-31", summary: "x" }), []);
});
