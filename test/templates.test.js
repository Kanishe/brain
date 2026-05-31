import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";
import yaml from "js-yaml";
import { validateManifest, validateNote } from "../brain_tools/schema.js";
import { read } from "../brain_tools/frontmatter.js";

const TEMPLATES = join(dirname(fileURLToPath(import.meta.url)), "..", "templates");

test("area template is a valid manifest", () => {
  const data = yaml.load(readFileSync(join(TEMPLATES, "area.brain.yml"), "utf-8"));
  assert.deepEqual(validateManifest(data), []);
});

test("note template is a valid note", () => {
  const { meta } = read(join(TEMPLATES, "note.md"));
  assert.deepEqual(validateNote(meta), []);
});
