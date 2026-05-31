package brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MigrateTest {
  @Test
  void migrateSetsCurrentVersion() {
    Map<String, Object> out = Migrate.migrateNote(Map.of("date", "2026-05-31", "summary", "x"));
    assertEquals(Schema.CURRENT_SCHEMA_VERSION, out.get("schema_version"));
  }

  @Test
  void migrateAddsMissingKeywords() {
    Map<String, Object> out =
        Migrate.migrateNote(Map.of("schema_version", 0, "date", "2026-05-31", "summary", "x"));
    assertEquals(List.of(), out.get("keywords"));
  }

  @Test
  void migratePreservesExistingFields() {
    Map<String, Object> out = Migrate.migrateNote(Map.of(
        "schema_version", 0, "date", "2026-05-31", "summary", "x",
        "keywords", List.of("auth"), "tags", List.of("java")));
    assertEquals(List.of("auth"), out.get("keywords"));
    assertEquals(List.of("java"), out.get("tags"));
  }

  @Test
  void needsMigrationDetectsOldVersion() {
    assertTrue(Migrate.needsMigration(Map.of("schema_version", 0)));
    assertFalse(Migrate.needsMigration(Map.of("schema_version", 1)));
  }
}
