import test from "node:test";
import assert from "node:assert/strict";
import { mkdtempSync, mkdirSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { search } from "../brain_tools/recall.js";

function tmp() {
  return mkdtempSync(join(tmpdir(), "rec-"));
}

function area(root, name, sensitivity) {
  const dir = join(root, name);
  mkdirSync(dir, { recursive: true });
  writeFileSync(
    join(dir, ".brain.yml"),
    `schema_version: 1\narea: ${name}\nsensitivity: ${sensitivity}\n`,
    "utf-8"
  );
  return dir;
}

function note(dir, name, summary, keywords) {
  writeFileSync(
    join(dir, name),
    `---\nschema_version: 1\ndate: 2026-05-31\nsummary: ${summary}\nkeywords: [${keywords.join(", ")}]\n---\nbody\n`,
    "utf-8"
  );
}

test("recall matches by keyword", () => {
  const root = tmp();
  const work = area(root, "work", "confidential");
  note(work, "2026-05-31_auth.md", "Auth service", ["авторизация", "jwt"]);
  note(work, "2026-05-30_billing.md", "Billing", ["платежи"]);
  const results = search(root, "сервис авторизация");
  assert.equal(results[0].name, "2026-05-31_auth");
});

test("recall respects maxResults", () => {
  const root = tmp();
  const work = area(root, "work", "personal");
  for (let i = 0; i < 5; i++) {
    note(work, `2026-05-0${i}_n${i}.md`, `Note ${i}`, ["auth"]);
  }
  const results = search(root, "auth", { maxResults: 3 });
  assert.equal(results.length, 3);
});

test("recall tolerates punctuation in the query", () => {
  const root = tmp();
  const work = area(root, "work", "personal");
  note(work, "2026-05-31_auth.md", "Auth service", ["авторизация", "jwt"]);
  const results = search(root, "нужен auth-сервис, как jwt?");
  assert.equal(results[0].name, "2026-05-31_auth");
});

test("recall privacy filter excludes higher sensitivity", () => {
  const root = tmp();
  const work = area(root, "work", "confidential");
  const live = area(root, "live", "personal");
  note(work, "2026-05-31_auth.md", "Work auth", ["auth"]);
  note(live, "2026-05-31_home.md", "Home auth", ["auth"]);
  const results = search(root, "auth", { intoSensitivity: "personal" });
  const names = results.map((r) => r.name);
  assert.ok(names.includes("2026-05-31_home"));
  assert.ok(!names.includes("2026-05-31_auth"));
});
