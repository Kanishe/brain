import test from "node:test";
import assert from "node:assert/strict";
import { mkdtempSync, writeFileSync, existsSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { main } from "../brain_tools/cli.js";

function tmp() {
  return mkdtempSync(join(tmpdir(), "cli-"));
}

function capture() {
  const lines = [];
  return { log: (s) => lines.push(String(s)), out: () => lines.join("\n") };
}

test("cli init creates manifest", () => {
  const root = tmp();
  const area = join(root, "work");
  const cap = capture();
  const code = main(
    ["init", area, "--area", "work", "--sensitivity", "confidential"],
    { log: cap.log }
  );
  assert.equal(code, 0);
  assert.ok(existsSync(join(area, ".brain.yml")));
});

test("cli recall outputs JSON", () => {
  const root = tmp();
  const work = join(root, "work");
  main(["init", work, "--area", "work", "--sensitivity", "personal"], {
    log: () => {},
  });
  writeFileSync(
    join(work, "2026-05-31_auth.md"),
    "---\nschema_version: 1\ndate: 2026-05-31\nsummary: Auth service\nkeywords: [авторизация]\n---\nbody\n",
    "utf-8"
  );
  const cap = capture();
  const code = main(["recall", root, "авторизация"], { log: cap.log });
  assert.equal(code, 0);
  const parsed = JSON.parse(cap.out());
  assert.equal(parsed[0].name, "2026-05-31_auth");
});

test("cli validate reports errors", () => {
  const root = tmp();
  const bad = join(root, ".brain.yml");
  writeFileSync(bad, "schema_version: 1\n", "utf-8"); // missing area
  const cap = capture();
  const code = main(["validate", bad], { log: cap.log });
  assert.equal(code, 1);
  assert.ok(cap.out().includes("area is required"));
});
