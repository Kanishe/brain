package brain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Schema constants and validation for manifests and notes. */
public final class Schema {
  public static final int CURRENT_SCHEMA_VERSION = 1;
  public static final Map<String, Integer> SENSITIVITY_LEVELS =
      Map.of("public", 0, "personal", 1, "confidential", 2);
  public static final String DEFAULT_SENSITIVITY = "personal";

  private Schema() {}

  public static int level(String sensitivity) {
    return SENSITIVITY_LEVELS.get(sensitivity);
  }

  /**
   * True if content from the source area may be written into the target area.
   * Content may only move into an area at least as protected as its source.
   * Unknown sensitivity fails closed (returns false).
   */
  public static boolean canCopy(String source, String target) {
    Integer s = SENSITIVITY_LEVELS.get(source);
    Integer t = SENSITIVITY_LEVELS.get(target);
    if (s == null || t == null) {
      return false;
    }
    return t >= s;
  }

  public static List<String> validateManifest(Map<String, Object> data) {
    List<String> errors = new ArrayList<>();
    if (!Integer.valueOf(CURRENT_SCHEMA_VERSION).equals(data.get("schema_version"))) {
      errors.add("schema_version must be " + CURRENT_SCHEMA_VERSION);
    }
    Object area = data.get("area");
    if (area == null || area.toString().isEmpty()) {
      errors.add("area is required");
    }
    Object sensitivity = data.getOrDefault("sensitivity", DEFAULT_SENSITIVITY);
    if (!SENSITIVITY_LEVELS.containsKey(sensitivity)) {
      errors.add("sensitivity must be one of " + sortedLevels());
    }
    return errors;
  }

  public static List<String> validateNote(Map<String, Object> data) {
    List<String> errors = new ArrayList<>();
    if (!Integer.valueOf(CURRENT_SCHEMA_VERSION).equals(data.get("schema_version"))) {
      errors.add("schema_version must be " + CURRENT_SCHEMA_VERSION);
    }
    if (isBlank(data.get("date"))) {
      errors.add("date is required");
    }
    if (isBlank(data.get("summary"))) {
      errors.add("summary is required");
    }
    return errors;
  }

  private static boolean isBlank(Object value) {
    return value == null || value.toString().isEmpty();
  }

  private static List<String> sortedLevels() {
    List<String> keys = new ArrayList<>(SENSITIVITY_LEVELS.keySet());
    Collections.sort(keys);
    return keys;
  }
}
