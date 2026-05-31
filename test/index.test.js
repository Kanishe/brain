import test from "node:test";
import assert from "node:assert/strict";
import { mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join, basename } from "node:path";
import * as index from "../brain_tools/index.js";

function tmp() {
  return mkdtempSync(join(tmpdir(), "idx-"));
}

function note(dir, name, date, summary) {
  writeFileSync(
    join(dir, name),
    `---\nschema_version: 1\ndate: ${date}\nsummary: ${summary}\n---\nbody\n`,
    "utf-8"
  );
}

test("rebuild lists notes newest first", () => {
  const dir = tmp();
  note(dir, "2026-05-01_alpha.md", "2026-05-01", "Alpha note");
  note(dir, "2026-05-31_beta.md", "2026-05-31", "Beta note");
  const content = index.rebuild(dir);
  assert.ok(content.indexOf("beta") < content.indexOf("alpha"));
  assert.ok(content.includes("Alpha note"));
  assert.ok(content.includes("Beta note"));
});

test("rebuild excludes MOC itself", () => {
  const dir = tmp();
  note(dir, "2026-05-01_alpha.md", "2026-05-01", "Alpha note");
  writeFileSync(join(dir, "MOC.md"), "stale\n", "utf-8");
  const content = index.rebuild(dir);
  assert.ok(!content.includes("[[MOC]]"));
  assert.ok(content.includes("[[2026-05-01_alpha]]"));
});

test("write creates MOC file", () => {
  const dir = tmp();
  note(dir, "2026-05-01_alpha.md", "2026-05-01", "Alpha note");
  const target = index.write(dir);
  assert.equal(basename(target), "MOC.md");
});
