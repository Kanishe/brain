// Schema constants and validation for manifests and notes.

export const CURRENT_SCHEMA_VERSION = 1;

export const SENSITIVITY_LEVELS = { public: 0, personal: 1, confidential: 2 };
export const DEFAULT_SENSITIVITY = "personal";

export function level(sensitivity) {
  return SENSITIVITY_LEVELS[sensitivity];
}

// True if content from source area may be written into target area.
// Content may only move into an area at least as protected as its source.
export function canCopy(sourceSensitivity, targetSensitivity) {
  return level(targetSensitivity) >= level(sourceSensitivity);
}

export function validateManifest(data) {
  const errors = [];
  if (data.schema_version !== CURRENT_SCHEMA_VERSION) {
    errors.push(`schema_version must be ${CURRENT_SCHEMA_VERSION}`);
  }
  if (!data.area) {
    errors.push("area is required");
  }
  const sensitivity = data.sensitivity ?? DEFAULT_SENSITIVITY;
  if (!(sensitivity in SENSITIVITY_LEVELS)) {
    errors.push(`sensitivity must be one of ${Object.keys(SENSITIVITY_LEVELS).sort().join(", ")}`);
  }
  return errors;
}

export function validateNote(data) {
  const errors = [];
  if (data.schema_version !== CURRENT_SCHEMA_VERSION) {
    errors.push(`schema_version must be ${CURRENT_SCHEMA_VERSION}`);
  }
  if (!data.date) {
    errors.push("date is required");
  }
  if (!data.summary) {
    errors.push("summary is required");
  }
  return errors;
}
