package brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InitTest {
  @Test
  void initCreatesManifestAndIndex(@TempDir Path root) throws Exception {
    Path area = root.resolve("work");
    Init.initArea(area, "work", "confidential", "", "<topic>", List.of());
    Map<String, Object> manifest = Yamls.load(Files.readString(area.resolve(".brain.yml")));
    assertEquals("work", manifest.get("area"));
    assertEquals("confidential", manifest.get("sensitivity"));
    assertEquals(Schema.CURRENT_SCHEMA_VERSION, manifest.get("schema_version"));
    assertTrue(Files.exists(area.resolve("MOC.md")));
  }

  @Test
  void initStoresDescriptionLayoutKeywords(@TempDir Path root) throws Exception {
    Path area = root.resolve("education");
    Init.initArea(area, "education", "personal", "Обучение",
        "<it|english>/<topic>", List.of("обучение", "it"));
    Map<String, Object> m = Yamls.load(Files.readString(area.resolve(".brain.yml")));
    assertEquals("Обучение", m.get("description"));
    assertEquals("<it|english>/<topic>", m.get("layout"));
    assertEquals(List.of("обучение", "it"), m.get("keywords"));
  }

  @Test
  void initRejectsInvalidSensitivity(@TempDir Path root) {
    Path area = root.resolve("work");
    assertThrows(IllegalArgumentException.class,
        () -> Init.initArea(area, "work", "secret", "", "<topic>", List.of()));
  }
}
