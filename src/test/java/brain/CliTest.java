package brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CliTest {
  private static PrintStream devnull() {
    return new PrintStream(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8);
  }

  private static PrintStream printer(ByteArrayOutputStream sink) {
    return new PrintStream(sink, true, StandardCharsets.UTF_8);
  }

  @Test
  void cliInitCreatesManifest(@TempDir Path root) throws Exception {
    Path area = root.resolve("work");
    int code = Cli.run(
        new String[] {"init", area.toString(), "--area", "work", "--sensitivity", "confidential"},
        devnull());
    assertEquals(0, code);
    assertTrue(Files.exists(area.resolve(".brain.yml")));
  }

  @Test
  void cliInitAcceptsDescriptionLayoutKeywords(@TempDir Path root) throws Exception {
    Path area = root.resolve("education");
    int code = Cli.run(
        new String[] {
          "init", area.toString(), "--area", "education", "--sensitivity", "personal",
          "--description", "Обучение", "--layout", "<it|english>/<topic>",
          "--keywords", "обучение, it, english"
        },
        devnull());
    assertEquals(0, code);
    Map<String, Object> m = Yamls.load(Files.readString(area.resolve(".brain.yml")));
    assertEquals("Обучение", m.get("description"));
    assertEquals("<it|english>/<topic>", m.get("layout"));
    assertEquals(List.of("обучение", "it", "english"), m.get("keywords"));
  }

  @Test
  void cliRecallOutputsJson(@TempDir Path root) throws Exception {
    Path work = root.resolve("work");
    Cli.run(new String[] {"init", work.toString(), "--area", "work", "--sensitivity", "personal"},
        devnull());
    Files.writeString(work.resolve("2026-05-31_auth.md"),
        "---\nschema_version: 1\ndate: 2026-05-31\nsummary: Auth service\nkeywords: [авторизация]\n---\nbody\n");
    ByteArrayOutputStream sink = new ByteArrayOutputStream();
    int code = Cli.run(new String[] {"recall", root.toString(), "авторизация"}, printer(sink));
    assertEquals(0, code);
    assertTrue(sink.toString(StandardCharsets.UTF_8).contains("\"name\": \"2026-05-31_auth\""));
  }

  @Test
  void cliIndexRefusesDirectoryWithoutManifest(@TempDir Path root) throws Exception {
    Path notAnArea = root.resolve("brain");
    Files.createDirectories(notAnArea.resolve("work"));
    Files.writeString(notAnArea.resolve("work/2026-05-31_secret.md"),
        "---\nschema_version: 1\ndate: 2026-05-31\nsummary: Secret\n---\nbody\n");
    ByteArrayOutputStream sink = new ByteArrayOutputStream();
    int code = Cli.run(new String[] {"index", notAnArea.toString()}, printer(sink));
    assertEquals(1, code);
    assertTrue(sink.toString(StandardCharsets.UTF_8).contains("not an area"));
    assertTrue(Files.notExists(notAnArea.resolve("MOC.md")));
  }

  @Test
  void cliValidateReportsErrors(@TempDir Path root) throws Exception {
    Path bad = root.resolve(".brain.yml");
    Files.writeString(bad, "schema_version: 1\n"); // missing area
    ByteArrayOutputStream sink = new ByteArrayOutputStream();
    int code = Cli.run(new String[] {"validate", bad.toString()}, printer(sink));
    assertEquals(1, code);
    assertTrue(sink.toString(StandardCharsets.UTF_8).contains("area is required"));
  }
}
