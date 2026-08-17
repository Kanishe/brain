package brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IndexTest {
  private static void note(Path dir, String name, String date, String summary) throws Exception {
    Files.writeString(dir.resolve(name),
        "---\nschema_version: 1\ndate: " + date + "\nsummary: " + summary + "\n---\nbody\n");
  }

  @Test
  void rebuildListsNotesNewestFirst(@TempDir Path dir) throws Exception {
    note(dir, "2026-05-01_alpha.md", "2026-05-01", "Alpha note");
    note(dir, "2026-05-31_beta.md", "2026-05-31", "Beta note");
    String content = Index.rebuild(dir);
    assertTrue(content.indexOf("beta") < content.indexOf("alpha"));
    assertTrue(content.contains("Alpha note"));
    assertTrue(content.contains("Beta note"));
  }

  @Test
  void rebuildExcludesMocItself(@TempDir Path dir) throws Exception {
    note(dir, "2026-05-01_alpha.md", "2026-05-01", "Alpha note");
    Files.writeString(dir.resolve("MOC.md"), "stale\n");
    String content = Index.rebuild(dir);
    assertTrue(!content.contains("[[MOC]]"));
    assertTrue(content.contains("[[2026-05-01_alpha]]"));
  }

  @Test
  void writeCreatesMocFile(@TempDir Path dir) throws Exception {
    note(dir, "2026-05-01_alpha.md", "2026-05-01", "Alpha note");
    Path target = Index.write(dir);
    assertEquals("MOC.md", target.getFileName().toString());
  }

  @Test
  void rebuildFindsNotesInNestedTopicFolders(@TempDir Path dir) throws Exception {
    Path nested = dir.resolve("it/language/python/environment");
    Files.createDirectories(nested);
    note(nested, "2026-08-17_env-setup.md", "2026-08-17", "Env setup note");
    String content = Index.rebuild(dir);
    assertTrue(content.contains("[[2026-08-17_env-setup]]"));
    assertTrue(content.contains("Env setup note"));
  }
}
