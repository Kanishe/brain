package brain;

import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/** YAML load/dump helpers shared across the brain tools. */
final class Yamls {
  private Yamls() {}

  @SuppressWarnings("unchecked")
  static Map<String, Object> load(String text) {
    Object parsed = new Yaml().load(text);
    return (parsed instanceof Map) ? (Map<String, Object>) parsed : new LinkedHashMap<>();
  }

  static String dump(Map<String, Object> data) {
    DumperOptions opts = new DumperOptions();
    opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    opts.setAllowUnicode(true);
    return new Yaml(opts).dump(data);
  }
}
