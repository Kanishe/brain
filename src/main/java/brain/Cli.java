package brain;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Command-line entry point: validate / index / recall / init. */
public final class Cli {
  private static final String MANIFEST_NAME = ".brain.yml";

  private Cli() {}

  public static int run(String[] argv, PrintStream out) throws IOException {
    if (argv.length == 0) {
      out.println("usage: brain <init|index|recall|validate> ...");
      return 2;
    }
    String[] rest = Arrays.copyOfRange(argv, 1, argv.length);
    return switch (argv[0]) {
      case "init" -> cmdInit(rest, out);
      case "index" -> cmdIndex(rest, out);
      case "recall" -> cmdRecall(rest, out);
      case "validate" -> cmdValidate(rest, out);
      default -> {
        out.println("unknown command: " + argv[0]);
        yield 2;
      }
    };
  }

  private record Args(List<String> positional, Map<String, String> flags) {}

  private static Args parse(String[] rest) {
    List<String> positional = new ArrayList<>();
    Map<String, String> flags = new HashMap<>();
    for (int i = 0; i < rest.length; i++) {
      String a = rest[i];
      if (a.startsWith("--")) {
        flags.put(a.substring(2), i + 1 < rest.length ? rest[i + 1] : null);
        i++;
      } else {
        positional.add(a);
      }
    }
    return new Args(positional, flags);
  }

  private static int cmdInit(String[] rest, PrintStream out) throws IOException {
    Args a = parse(rest);
    List<String> keywords = new ArrayList<>();
    String kw = a.flags().get("keywords");
    if (kw != null) {
      for (String k : kw.split(",")) {
        String trimmed = k.strip();
        if (!trimmed.isEmpty()) {
          keywords.add(trimmed);
        }
      }
    }
    String path = a.positional().get(0);
    Init.initArea(
        Path.of(path),
        a.flags().get("area"),
        a.flags().getOrDefault("sensitivity", Schema.DEFAULT_SENSITIVITY),
        a.flags().getOrDefault("description", ""),
        a.flags().getOrDefault("layout", "<topic>"),
        keywords);
    out.println("initialized area at " + path);
    return 0;
  }

  private static int cmdIndex(String[] rest, PrintStream out) throws IOException {
    Args a = parse(rest);
    Path dir = Path.of(a.positional().get(0));
    if (!Files.exists(dir.resolve(MANIFEST_NAME))) {
      out.println("refusing: " + dir + " has no " + MANIFEST_NAME + " — not an area. "
          + "brain index walks the whole subtree, so running it on a non-area directory "
          + "(e.g. the brain root) can leak notes from other areas into one MOC.md.");
      return 1;
    }
    Path target = Index.write(dir);
    out.println("wrote " + target);
    return 0;
  }

  private static int cmdRecall(String[] rest, PrintStream out) throws IOException {
    Args a = parse(rest);
    List<String> positional = a.positional();
    String brainRoot = positional.get(0);
    String query = String.join(" ", positional.subList(1, positional.size()));
    int maxResults = a.flags().get("max-results") != null
        ? Integer.parseInt(a.flags().get("max-results"))
        : 3;
    String into = a.flags().get("into");
    List<Recall.Candidate> results = Recall.search(Path.of(brainRoot), query, maxResults, into);
    out.println(toJson(results));
    return 0;
  }

  private static int cmdValidate(String[] rest, PrintStream out) throws IOException {
    Args a = parse(rest);
    Path path = Path.of(a.positional().get(0));
    Map<String, Object> data = Yamls.load(Files.readString(path));
    List<String> errors = path.getFileName().toString().equals(".brain.yml")
        ? Schema.validateManifest(data)
        : Schema.validateNote(data);
    for (String e : errors) {
      out.println(e);
    }
    return errors.isEmpty() ? 0 : 1;
  }

  private static String toJson(List<Recall.Candidate> results) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < results.size(); i++) {
      Recall.Candidate c = results.get(i);
      sb.append(i > 0 ? "," : "").append("\n  {")
          .append("\"name\": ").append(jstr(c.name())).append(", ")
          .append("\"path\": ").append(jstr(c.path())).append(", ")
          .append("\"summary\": ").append(jstr(c.summary())).append(", ")
          .append("\"area\": ").append(jstr(c.area())).append(", ")
          .append("\"sensitivity\": ").append(jstr(c.sensitivity())).append(", ")
          .append("\"score\": ").append(c.score())
          .append("}");
    }
    sb.append(results.isEmpty() ? "]" : "\n]");
    return sb.toString();
  }

  private static String jstr(String s) {
    StringBuilder b = new StringBuilder("\"");
    for (char ch : s.toCharArray()) {
      switch (ch) {
        case '"' -> b.append("\\\"");
        case '\\' -> b.append("\\\\");
        case '\n' -> b.append("\\n");
        case '\r' -> b.append("\\r");
        case '\t' -> b.append("\\t");
        default -> b.append(ch);
      }
    }
    return b.append("\"").toString();
  }

  public static void main(String[] args) throws IOException {
    System.exit(run(args, System.out));
  }
}
