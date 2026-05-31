package brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SchemaTest {
  @Test
  void currentVersionIsOne() {
    assertEquals(1, Schema.CURRENT_SCHEMA_VERSION);
  }

  @Test
  void sensitivityOrdering() {
    assertTrue(Schema.level("public") < Schema.level("personal"));
    assertTrue(Schema.level("personal") < Schema.level("confidential"));
  }

  @Test
  void canCopyAllowsEqualOrLowerSource() {
    assertTrue(Schema.canCopy("personal", "confidential"));
    assertTrue(Schema.canCopy("personal", "personal"));
  }

  @Test
  void canCopyBlocksHigherSourceIntoLowerTarget() {
    assertFalse(Schema.canCopy("confidential", "personal"));
  }

  @Test
  void validateManifestAcceptsMinimalValid() {
    assertEquals(List.of(), Schema.validateManifest(Map.of("schema_version", 1, "area", "work")));
  }

  @Test
  void validateManifestReportsMissingArea() {
    assertTrue(Schema.validateManifest(Map.of("schema_version", 1)).stream()
        .anyMatch(e -> e.contains("area")));
  }

  @Test
  void validateManifestRejectsUnknownSensitivity() {
    assertTrue(Schema.validateManifest(
            Map.of("schema_version", 1, "area", "work", "sensitivity", "secret"))
        .stream().anyMatch(e -> e.contains("sensitivity")));
  }

  @Test
  void validateNoteRequiresDateAndSummary() {
    List<String> errors = Schema.validateNote(Map.of("schema_version", 1));
    assertTrue(errors.stream().anyMatch(e -> e.contains("date")));
    assertTrue(errors.stream().anyMatch(e -> e.contains("summary")));
  }

  @Test
  void validateNoteAcceptsValid() {
    assertEquals(List.of(),
        Schema.validateNote(Map.of("schema_version", 1, "date", "2026-05-31", "summary", "x")));
  }
}
