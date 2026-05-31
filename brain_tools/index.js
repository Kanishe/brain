// Rebuild a human-readable MOC.md index from note frontmatter (self-healing).

import { readdirSync, writeFileSync } from "node:fs";
import { join, basename } from "node:path";
import { read } from "./frontmatter.js";

export const INDEX_NAME = "MOC.md";

// Return MOC.md content rebuilt from the notes in areaDir.
export function rebuild(areaDir) {
  const entries = [];
  const files = readdirSync(areaDir)
    .filter((f) => f.endsWith(".md") && f !== INDEX_NAME)
    .sort();
  for (const f of files) {
    const { meta } = read(join(areaDir, f));
    entries.push({
      date: String(meta.date ?? ""),
      name: f.replace(/\.md$/, ""),
      summary: meta.summary ?? "",
    });
  }
  // newest date first, then name descending for stable ordering
  entries.sort((a, b) => {
    if (a.date !== b.date) return a.date < b.date ? 1 : -1;
    return a.name < b.name ? 1 : -1;
  });
  const lines = [
    "---",
    "type: index",
    "generated: true",
    "---",
    "",
    `# Index — ${basename(areaDir)}`,
    "",
  ];
  for (const e of entries) {
    lines.push(`- [[${e.name}]] — ${e.summary}`);
  }
  return lines.join("\n") + "\n";
}

// Write MOC.md into areaDir and return its path.
export function write(areaDir) {
  const target = join(areaDir, INDEX_NAME);
  writeFileSync(target, rebuild(areaDir), "utf-8");
  return target;
}
