package brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FrontmatterTest {
  @Test
  void readsFrontmatterAndBody(@TempDir Path dir) throws Exception {
    Path f = dir.resolve("note.md");
    Files.writeString(f,
        "---\nschema_version: 1\ndate: 2026-05-31\nsummary: hello\nkeywords: [auth, jwt]\n---\n# Body\n");
    Frontmatter.Doc doc = Frontmatter.read(f);
    assertEquals("hello", doc.meta().get("summary"));
    assertEquals(List.of("auth", "jwt"), doc.meta().get("keywords"));
    assertEquals("# Body", doc.body().strip());
  }

  @Test
  void missingFrontmatterReturnsEmptyMeta(@TempDir Path dir) throws Exception {
    Path f = dir.resolve("plain.md");
    Files.writeString(f, "# Just a title\n");
    Frontmatter.Doc doc = Frontmatter.read(f);
    assertTrue(doc.meta().isEmpty());
    assertTrue(doc.body().contains("Just a title"));
  }
}
