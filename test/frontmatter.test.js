import test from "node:test";
import assert from "node:assert/strict";
import { mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { read } from "../brain_tools/frontmatter.js";

function tmp() {
  return mkdtempSync(join(tmpdir(), "fm-"));
}

test("reads frontmatter and body", () => {
  const dir = tmp();
  const f = join(dir, "note.md");
  writeFileSync(
    f,
    "---\nschema_version: 1\ndate: 2026-05-31\nsummary: hello\nkeywords: [auth, jwt]\n---\n# Body\n",
    "utf-8"
  );
  const { meta, body } = read(f);
  assert.equal(meta.summary, "hello");
  assert.deepEqual(meta.keywords, ["auth", "jwt"]);
  assert.equal(body.trim(), "# Body");
});

test("missing frontmatter returns empty meta", () => {
  const dir = tmp();
  const f = join(dir, "plain.md");
  writeFileSync(f, "# Just a title\n", "utf-8");
  const { meta, body } = read(f);
  assert.deepEqual(meta, {});
  assert.ok(body.includes("Just a title"));
});
