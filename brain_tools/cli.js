#!/usr/bin/env node
// Command-line entry point: validate / index / recall / init.

import { readFileSync } from "node:fs";
import { basename } from "node:path";
import yaml from "js-yaml";
import { initArea } from "./init.js";
import { write as writeIndex } from "./index.js";
import { search } from "./recall.js";
import { validateManifest, validateNote, DEFAULT_SENSITIVITY } from "./schema.js";

function parseArgs(rest) {
  const positional = [];
  const flags = {};
  for (let i = 0; i < rest.length; i++) {
    const a = rest[i];
    if (a.startsWith("--")) {
      flags[a.slice(2)] = rest[i + 1];
      i++;
    } else {
      positional.push(a);
    }
  }
  return { positional, flags };
}

function cmdInit(rest, log) {
  const { positional, flags } = parseArgs(rest);
  initArea(positional[0], {
    areaName: flags.area,
    sensitivity: flags.sensitivity ?? DEFAULT_SENSITIVITY,
  });
  log(`initialized area at ${positional[0]}`);
  return 0;
}

function cmdIndex(rest, log) {
  const { positional } = parseArgs(rest);
  const target = writeIndex(positional[0]);
  log(`wrote ${target}`);
  return 0;
}

function cmdRecall(rest, log) {
  const { positional, flags } = parseArgs(rest);
  const [brainRoot, ...queryParts] = positional;
  const results = search(brainRoot, queryParts.join(" "), {
    maxResults: flags["max-results"] ? Number(flags["max-results"]) : 3,
    intoSensitivity: flags.into ?? null,
  });
  log(JSON.stringify(results, null, 2));
  return 0;
}

function cmdValidate(rest, log) {
  const { positional } = parseArgs(rest);
  const path = positional[0];
  const data = yaml.load(readFileSync(path, "utf-8")) || {};
  const errors =
    basename(path) === ".brain.yml" ? validateManifest(data) : validateNote(data);
  for (const e of errors) log(e);
  return errors.length ? 1 : 0;
}

export function main(argv, { log = console.log } = {}) {
  const [cmd, ...rest] = argv;
  switch (cmd) {
    case "init":
      return cmdInit(rest, log);
    case "index":
      return cmdIndex(rest, log);
    case "recall":
      return cmdRecall(rest, log);
    case "validate":
      return cmdValidate(rest, log);
    default:
      log(`unknown command: ${cmd}`);
      return 2;
  }
}

if (import.meta.url === `file://${process.argv[1]}`) {
  process.exit(main(process.argv.slice(2)));
}
