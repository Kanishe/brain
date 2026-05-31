// Read YAML frontmatter from Markdown files.

import { readFileSync } from "node:fs";
import yaml from "js-yaml";

const DELIM = "---";

// Return { meta, body }. meta is {} when there is no frontmatter.
export function read(path) {
  const text = readFileSync(path, "utf-8");
  const lines = text.split("\n");
  if (lines.length === 0 || lines[0].trim() !== DELIM) {
    return { meta: {}, body: text };
  }
  let closing = -1;
  for (let i = 1; i < lines.length; i++) {
    if (lines[i].trim() === DELIM) {
      closing = i;
      break;
    }
  }
  if (closing === -1) {
    return { meta: {}, body: text };
  }
  const raw = lines.slice(1, closing).join("\n");
  const meta = yaml.load(raw) || {};
  const body = lines.slice(closing + 1).join("\n");
  return { meta, body };
}
