// Keyword recall across brain areas, honoring the privacy boundary.

import { readdirSync, readFileSync } from "node:fs";
import { join, basename } from "node:path";
import yaml from "js-yaml";
import { read } from "./frontmatter.js";
import { DEFAULT_SENSITIVITY, canCopy } from "./schema.js";

const MANIFEST_NAME = ".brain.yml";
const INDEX_NAME = "MOC.md";

function tokenize(text) {
  return text
    .toLowerCase()
    .replace(/,/g, " ")
    .split(/\s+/)
    .filter(Boolean);
}

function areaSensitivity(areaDir) {
  try {
    const data = yaml.load(readFileSync(join(areaDir, MANIFEST_NAME), "utf-8")) || {};
    return data.sensitivity ?? DEFAULT_SENSITIVITY;
  } catch {
    return DEFAULT_SENSITIVITY;
  }
}

function walkMd(dir) {
  const out = [];
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const p = join(dir, entry.name);
    if (entry.isDirectory()) {
      out.push(...walkMd(p));
    } else if (entry.name.endsWith(".md") && entry.name !== INDEX_NAME) {
      out.push(p);
    }
  }
  return out;
}

function score(queryTokens, meta) {
  const hay = [
    String(meta.summary ?? ""),
    ...(meta.keywords ?? []).map(String),
    ...(meta.tags ?? []).map(String),
  ]
    .join(" ")
    .toLowerCase();
  return queryTokens.reduce((n, t) => n + (hay.includes(t) ? 1 : 0), 0);
}

// Return up to maxResults candidate notes ranked by keyword match.
// If intoSensitivity is set, exclude notes from areas more protected than the target.
export function search(brainRoot, query, { maxResults = 3, intoSensitivity = null } = {}) {
  const queryTokens = tokenize(query);
  const candidates = [];
  const areas = readdirSync(brainRoot, { withFileTypes: true })
    .filter((e) => e.isDirectory())
    .map((e) => e.name)
    .sort();
  for (const areaName of areas) {
    const areaDir = join(brainRoot, areaName);
    const sensitivity = areaSensitivity(areaDir);
    if (intoSensitivity !== null && !canCopy(sensitivity, intoSensitivity)) {
      continue;
    }
    for (const path of walkMd(areaDir).sort()) {
      const { meta } = read(path);
      const s = score(queryTokens, meta);
      if (s === 0) continue;
      candidates.push({
        name: basename(path).replace(/\.md$/, ""),
        path,
        summary: meta.summary ?? "",
        area: areaName,
        sensitivity,
        score: s,
      });
    }
  }
  candidates.sort((a, b) => {
    if (a.score !== b.score) return b.score - a.score;
    return a.name < b.name ? 1 : -1;
  });
  return candidates.slice(0, maxResults);
}
