package brain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

/** Read YAML frontmatter from Markdown files. */
public final class Frontmatter {
  private static final String DELIM = "---";

  private Frontmatter() {}

  public record Doc(Map<String, Object> meta, String body) {}

  /** Returns the parsed frontmatter and the body. meta is empty when there is none. */
  public static Doc read(Path path) throws IOException {
    String text = Files.readString(path);
    String[] lines = text.split("\n", -1);
    if (lines.length == 0 || !lines[0].strip().equals(DELIM)) {
      return new Doc(Map.of(), text);
    }
    int closing = -1;
    for (int i = 1; i < lines.length; i++) {
      if (lines[i].strip().equals(DELIM)) {
        closing = i;
        break;
      }
    }
    if (closing == -1) {
      return new Doc(Map.of(), text);
    }
    String raw = String.join("\n", Arrays.copyOfRange(lines, 1, closing));
    Map<String, Object> meta = Yamls.load(raw);
    String body = String.join("\n", Arrays.copyOfRange(lines, closing + 1, lines.length));
    return new Doc(meta, body);
  }
}
