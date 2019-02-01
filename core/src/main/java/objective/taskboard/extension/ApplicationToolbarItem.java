package objective.taskboard.extension;

public interface ApplicationToolbarItem {
    String getIconName();
    String getOnClickLink();
    String getIconDisplayName();
    boolean isVisible();
}
