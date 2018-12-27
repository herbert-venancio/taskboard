package objective.taskboard.it.config.project;

import static java.lang.String.format;
import static objective.taskboard.it.components.ClusterComponent.CLUSTER_TAG;
import static objective.taskboard.it.components.SnackBarComponent.SNACK_BAR_TAG;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import objective.taskboard.it.components.ButtonComponent;
import objective.taskboard.it.components.ClusterComponent;
import objective.taskboard.it.components.ClusterRecalculateModalComponent;
import objective.taskboard.it.components.SnackBarComponent;
import objective.taskboard.it.components.TabComponent;
import objective.taskboard.it.components.TabsRouterComponent;
import objective.taskboard.testUtils.ProjectInfo;

public class ProjectClusterConfiguration extends ProjectAdvancedConfigurationTab<ProjectClusterConfiguration> {

    public static final String CLUSTER_TAB_NAME = "Cluster";
    public static final String PROJECT_CLUSTER_CONFIGURATION_TAG = "tb-project-cluster";

    private ButtonComponent recalculate;
    private ClusterComponent cluster;
    private ButtonComponent save;
    private SnackBarComponent snackbar;

    public static TabsRouterComponent.TabFactory<ProjectClusterConfiguration> factory(ProjectInfo projectInfo) {
        return (webDriver, tab) -> new ProjectClusterConfiguration(webDriver, tab, projectInfo);
    }

    public ProjectClusterConfiguration(WebDriver driver, TabComponent<ProjectClusterConfiguration> tab, ProjectInfo projectInfo) {
        super(driver, tab, projectInfo);
        this.recalculate = new ButtonComponent(webDriver, cssSelector(PROJECT_CLUSTER_CONFIGURATION_TAG + " obj-toolbar button"));
        this.cluster = new ClusterComponent(webDriver, cssSelector(PROJECT_CLUSTER_CONFIGURATION_TAG + " " + CLUSTER_TAG));
        this.save = new ButtonComponent(webDriver, cssSelector(PROJECT_CLUSTER_CONFIGURATION_TAG + " #tb-project-cluster-save"));
        this.snackbar = new SnackBarComponent(webDriver, cssSelector(SNACK_BAR_TAG + "#tb-project-cluster-snackbar"));
    }

    public ProjectClusterConfiguration assertEffort(String issueType, String size, String expectedEffort) {
        cluster.assertEffort(issueType, size, expectedEffort);
        return this;
    }

    public ProjectClusterConfiguration setEffort(String issueType, String size, String effort) {
        cluster.setEffort(issueType, size, effort);
        return this;
    }

    public ProjectClusterConfiguration assertCycle(String issueType, String size, String expectedCycle) {
        cluster.assertCycle(issueType, size, expectedCycle);
        return this;
    }

    public ProjectClusterConfiguration setCycle(String issueType, String size, String cycle) {
        cluster.setCycle(issueType, size, cycle);
        return this;
    }

    public ProjectClusterConfiguration assertCurrentValueNewValueIsShown(String issueType, String tsize) {
        cluster.assertCurrentValueNewValueIsShown(issueType, tsize);
        return this;
    }

    public ProjectClusterConfiguration assertIsFromBaseCluster(String issueType, String size, Boolean isVisible) {
        By spanSelector = cssSelector(format("obj-expansion-panel[data-issue-type=\"%s\"] th[data-sizing=\"%s\"] .from-base-cluster", issueType, size));
        waitElementExistenceAndVisibilityIs(isVisible, spanSelector);
        return this;
    }

    public ProjectClusterConfiguration assertSaveDisabled(boolean disabled) {
        save.waitDisabledBe(disabled);
        return this;
    }

    public ProjectClusterConfiguration save() {
        save.click();
        return this;
    }

    public ProjectClusterConfiguration assertSnackbarSavedIsOpen() {
        snackbar.waitTitleToBe("Success");
        snackbar.waitDescriptionToBe("Project cluster saved");
        return this;
    }

    public ProjectClusterConfiguration assertSnackbarErrorIsOpen() {
        snackbar.waitTitleToBe("Error");
        snackbar.waitDescriptionToBe("Please review the form");
        return this;
    }

    public ClusterRecalculateModalComponent openRecalculate() {
        recalculate.click();
        return new ClusterRecalculateModalComponent(webDriver);
    }

}
