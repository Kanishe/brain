package brain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Scaffold a new brain area: manifest + empty self-healing index. */
public final class Init {
  private static final String MANIFEST_NAME = ".brain.yml";

  private Init() {}

  /** Creates areaDir with a valid .brain.yml manifest and a fresh MOC.md. */
  public static Path initArea(
      Path areaDir,
      String areaName,
      String sensitivity,
      String description,
      String layout,
      List<String> keywords)
      throws IOException {
    if (!Schema.SENSITIVITY_LEVELS.containsKey(sensitivity)) {
      throw new IllegalArgumentException("invalid sensitivity: " + sensitivity);
    }
    Files.createDirectories(areaDir);
    Map<String, Object> manifest = new LinkedHashMap<>();
    manifest.put("schema_version", Schema.CURRENT_SCHEMA_VERSION);
    manifest.put("area", areaName);
    manifest.put("description", description);
    manifest.put("layout", layout);
    manifest.put("index", Index.INDEX_NAME);
    manifest.put("sensitivity", sensitivity);
    manifest.put("keywords", keywords);
    Files.writeString(areaDir.resolve(MANIFEST_NAME), Yamls.dump(manifest));
    Index.write(areaDir);
    return areaDir;
  }
}
