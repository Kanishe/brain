package brain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Rebuild a human-readable MOC.md index from note frontmatter (self-healing). */
public final class Index {
  public static final String INDEX_NAME = "MOC.md";

  private Index() {}

  private record Entry(String name, String summary) {}

  /** Returns MOC.md content rebuilt from the notes in areaDir. */
  public static String rebuild(Path areaDir) throws IOException {
    List<Path> files;
    try (Stream<Path> stream = Files.walk(areaDir)) {
      files = stream
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().endsWith(".md")
              && !p.getFileName().toString().equals(INDEX_NAME))
          .sorted(Comparator.comparing(p -> p.getFileName().toString()))
          .collect(Collectors.toList());
    }
    List<Entry> entries = new ArrayList<>();
    for (Path f : files) {
      Map<String, Object> meta = Frontmatter.read(f).meta();
      String name = f.getFileName().toString().replaceAll("\\.md$", "");
      String summary = String.valueOf(meta.getOrDefault("summary", ""));
      entries.add(new Entry(name, summary));
    }
    // notes are named YYYY-MM-DD_topic, so a reverse sort by name is newest-first
    entries.sort(Comparator.comparing(Entry::name).reversed());

    StringBuilder sb = new StringBuilder();
    sb.append("---\n").append("type: index\n").append("generated: true\n").append("---\n\n");
    sb.append("# Index — ").append(areaDir.getFileName().toString()).append("\n\n");
    for (Entry e : entries) {
      sb.append("- [[").append(e.name()).append("]] — ").append(e.summary()).append("\n");
    }
    return sb.toString();
  }

  /** Writes MOC.md into areaDir and returns its path. */
  public static Path write(Path areaDir) throws IOException {
    Path target = areaDir.resolve(INDEX_NAME);
    Files.writeString(target, rebuild(areaDir));
    return target;
  }
}
