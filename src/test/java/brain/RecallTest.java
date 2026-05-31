package brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RecallTest {
  private static Path area(Path root, String name, String sensitivity) throws Exception {
    Path dir = root.resolve(name);
    Files.createDirectories(dir);
    Files.writeString(dir.resolve(".brain.yml"),
        "schema_version: 1\narea: " + name + "\nsensitivity: " + sensitivity + "\n");
    return dir;
  }

  private static void note(Path dir, String name, String summary, String keywords) throws Exception {
    Files.writeString(dir.resolve(name),
        "---\nschema_version: 1\ndate: 2026-05-31\nsummary: " + summary
            + "\nkeywords: [" + keywords + "]\n---\nbody\n");
  }

  @Test
  void recallMatchesByKeyword(@TempDir Path root) throws Exception {
    Path work = area(root, "work", "confidential");
    note(work, "2026-05-31_auth.md", "Auth service", "авторизация, jwt");
    note(work, "2026-05-30_billing.md", "Billing", "платежи");
    List<Recall.Candidate> results = Recall.search(root, "сервис авторизация", 3, null);
    assertEquals("2026-05-31_auth", results.get(0).name());
  }

  @Test
  void recallRespectsMaxResults(@TempDir Path root) throws Exception {
    Path work = area(root, "work", "personal");
    for (int i = 0; i < 5; i++) {
      note(work, "2026-05-0" + i + "_n" + i + ".md", "Note " + i, "auth");
    }
    List<Recall.Candidate> results = Recall.search(root, "auth", 3, null);
    assertEquals(3, results.size());
  }

  @Test
  void recallToleratesPunctuationInQuery(@TempDir Path root) throws Exception {
    Path work = area(root, "work", "personal");
    note(work, "2026-05-31_auth.md", "Auth service", "авторизация, jwt");
    List<Recall.Candidate> results = Recall.search(root, "нужен auth-сервис, как jwt?", 3, null);
    assertEquals("2026-05-31_auth", results.get(0).name());
  }

  @Test
  void recallPrivacyFilterExcludesHigherSensitivity(@TempDir Path root) throws Exception {
    Path work = area(root, "work", "confidential");
    Path live = area(root, "live", "personal");
    note(work, "2026-05-31_auth.md", "Work auth", "auth");
    note(live, "2026-05-31_home.md", "Home auth", "auth");
    List<Recall.Candidate> results = Recall.search(root, "auth", 3, "personal");
    List<String> names = results.stream().map(Recall.Candidate::name).toList();
    assertTrue(names.contains("2026-05-31_home"));
    assertFalse(names.contains("2026-05-31_auth"));
  }
}
