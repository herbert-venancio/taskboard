package objective.taskboard.sizingImport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class SheetColumnDefinition {
    private final String name;
    private final PreviewBehavior previewBehavior;
    private final Map<String, String> tags;

    public SheetColumnDefinition(String name, PreviewBehavior previewBehavior, ColumnTag... tags) {
        this.name = requireNonNull(name);
        this.previewBehavior = requireNonNull(previewBehavior);
        this.tags = Arrays.stream(tags).collect(toMap(t -> t.key, t -> t.value));
    }

    public SheetColumnDefinition(String name, ColumnTag... tags) {
        this(name, PreviewBehavior.SHOW, tags);
    }

    public String getId() {
        return null;
    }

    public String getName() {
        return name;
    }
    
    public boolean isVisibleInPreview() {
        return previewBehavior == PreviewBehavior.SHOW;
    }
    
    public boolean hasTag(String tagKey) {
        return tags.containsKey(tagKey);
    }
    
    public boolean hasTag(String tagKey, String tagValue) {
        return hasTag(tagKey) && Objects.equals(tags.get(tagKey), tagValue);
    }

    public String getTagValue(String tag) {
        return tags.get(tag);
    }

    public enum PreviewBehavior {
        SHOW, HIDE;
    }
    
    public static class ColumnTag {
        private final String key;
        private final String value;

        public ColumnTag(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public ColumnTag(String key) {
            this(key, null);
        }
    }
}
