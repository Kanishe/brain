// Scaffold a new brain area: manifest + empty self-healing index.

import { mkdirSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import yaml from "js-yaml";
import {
  CURRENT_SCHEMA_VERSION,
  SENSITIVITY_LEVELS,
  DEFAULT_SENSITIVITY,
} from "./schema.js";
import { INDEX_NAME, write as writeIndex } from "./index.js";

const MANIFEST_NAME = ".brain.yml";

// Create areaDir with a valid .brain.yml manifest and a fresh MOC.md.
export function initArea(areaDir, { areaName, sensitivity = DEFAULT_SENSITIVITY } = {}) {
  if (!(sensitivity in SENSITIVITY_LEVELS)) {
    throw new Error(`invalid sensitivity: ${sensitivity}`);
  }
  mkdirSync(areaDir, { recursive: true });
  const manifest = {
    schema_version: CURRENT_SCHEMA_VERSION,
    area: areaName,
    description: "",
    layout: "<topic>",
    index: INDEX_NAME,
    sensitivity,
    keywords: [],
  };
  writeFileSync(
    join(areaDir, MANIFEST_NAME),
    yaml.dump(manifest, { sortKeys: false }),
    "utf-8"
  );
  writeIndex(areaDir);
  return areaDir;
}
