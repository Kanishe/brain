package brain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Migrate note frontmatter to the current schema version. */
public final class Migrate {
  private Migrate() {}

  public static boolean needsMigration(Map<String, Object> meta) {
    return !Integer.valueOf(Schema.CURRENT_SCHEMA_VERSION).equals(meta.get("schema_version"));
  }

  /** Returns a new metadata map upgraded to the current schema version (input untouched). */
  public static Map<String, Object> migrateNote(Map<String, Object> meta) {
    Map<String, Object> out = new LinkedHashMap<>(meta);
    out.putIfAbsent("keywords", new ArrayList<>());
    out.put("schema_version", Schema.CURRENT_SCHEMA_VERSION);
    return out;
  }
}
