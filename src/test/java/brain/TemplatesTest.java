package brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplatesTest {
  @Test
  void areaTemplateIsValidManifest() throws Exception {
    Map<String, Object> data = Yamls.load(Files.readString(Path.of("templates/area.brain.yml")));
    assertEquals(List.of(), Schema.validateManifest(data));
  }

  @Test
  void noteTemplateIsValidNote() throws Exception {
    Map<String, Object> meta = Frontmatter.read(Path.of("templates/note.md")).meta();
    assertEquals(List.of(), Schema.validateNote(meta));
  }
}
