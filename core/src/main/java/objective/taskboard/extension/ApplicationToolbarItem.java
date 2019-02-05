package objective.taskboard.extension;

public interface ApplicationToolbarItem {
    String getButtonId();
    String getIconName();
    String getOnClickLink();
    String getIconDisplayName();
    boolean isVisible();
}
