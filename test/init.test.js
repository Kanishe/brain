import test from "node:test";
import assert from "node:assert/strict";
import { mkdtempSync, readFileSync, existsSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import yaml from "js-yaml";
import { initArea } from "../brain_tools/init.js";
import { CURRENT_SCHEMA_VERSION } from "../brain_tools/schema.js";

function tmp() {
  return mkdtempSync(join(tmpdir(), "init-"));
}

test("init creates manifest and index", () => {
  const area = join(tmp(), "work");
  initArea(area, { areaName: "work", sensitivity: "confidential" });
  const manifest = yaml.load(readFileSync(join(area, ".brain.yml"), "utf-8"));
  assert.equal(manifest.area, "work");
  assert.equal(manifest.sensitivity, "confidential");
  assert.equal(manifest.schema_version, CURRENT_SCHEMA_VERSION);
  assert.ok(existsSync(join(area, "MOC.md")));
});

test("init rejects invalid sensitivity", () => {
  const area = join(tmp(), "work");
  assert.throws(
    () => initArea(area, { areaName: "work", sensitivity: "secret" }),
    /invalid sensitivity/
  );
});
