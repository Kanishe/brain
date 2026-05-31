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

/** Keyword recall across brain areas, honoring the privacy boundary. */
public final class Recall {
  private static final String MANIFEST_NAME = ".brain.yml";

  private Recall() {}

  public record Candidate(
      String name, String path, String summary, String area, String sensitivity, int score) {}

  /**
   * Returns up to maxResults candidate notes ranked by keyword match. When
   * intoSensitivity is non-null, areas more protected than the target are skipped.
   */
  public static List<Candidate> search(
      Path brainRoot, String query, int maxResults, String intoSensitivity) throws IOException {
    List<String> tokens = tokenize(query);
    List<Candidate> candidates = new ArrayList<>();

    List<Path> areas;
    try (Stream<Path> stream = Files.list(brainRoot)) {
      areas = stream.filter(Files::isDirectory)
          .sorted(Comparator.comparing(p -> p.getFileName().toString()))
          .collect(Collectors.toList());
    }

    for (Path areaDir : areas) {
      String sensitivity = areaSensitivity(areaDir);
      if (intoSensitivity != null && !Schema.canCopy(sensitivity, intoSensitivity)) {
        continue;
      }
      for (Path p : walkMd(areaDir)) {
        Map<String, Object> meta = Frontmatter.read(p).meta();
        int sc = score(tokens, meta);
        if (sc == 0) {
          continue;
        }
        String name = p.getFileName().toString().replaceAll("\\.md$", "");
        candidates.add(new Candidate(
            name, p.toString(), String.valueOf(meta.getOrDefault("summary", "")),
            areaDir.getFileName().toString(), sensitivity, sc));
      }
    }

    candidates.sort(Comparator.comparingInt(Candidate::score).reversed()
        .thenComparing(Comparator.comparing(Candidate::name).reversed()));
    return candidates.stream().limit(maxResults).collect(Collectors.toList());
  }

  private static List<String> tokenize(String text) {
    List<String> out = new ArrayList<>();
    for (String part : text.toLowerCase().split("[^\\p{L}\\p{N}]+")) {
      if (!part.isEmpty()) {
        out.add(part);
      }
    }
    return out;
  }

  private static String areaSensitivity(Path areaDir) {
    try {
      Map<String, Object> data = Yamls.load(Files.readString(areaDir.resolve(MANIFEST_NAME)));
      Object s = data.getOrDefault("sensitivity", Schema.DEFAULT_SENSITIVITY);
      return s == null ? Schema.DEFAULT_SENSITIVITY : s.toString();
    } catch (IOException e) {
      return Schema.DEFAULT_SENSITIVITY;
    }
  }

  private static List<Path> walkMd(Path dir) throws IOException {
    try (Stream<Path> stream = Files.walk(dir)) {
      return stream.filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().endsWith(".md")
              && !p.getFileName().toString().equals(Index.INDEX_NAME))
          .sorted()
          .collect(Collectors.toList());
    }
  }

  private static int score(List<String> tokens, Map<String, Object> meta) {
    StringBuilder hay = new StringBuilder();
    hay.append(meta.getOrDefault("summary", "")).append(' ');
    appendList(hay, meta.get("keywords"));
    appendList(hay, meta.get("tags"));
    String haystack = hay.toString().toLowerCase();
    int n = 0;
    for (String token : tokens) {
      if (haystack.contains(token)) {
        n++;
      }
    }
    return n;
  }

  private static void appendList(StringBuilder sb, Object value) {
    if (value instanceof List<?> list) {
      for (Object item : list) {
        sb.append(item).append(' ');
      }
    }
  }
}
