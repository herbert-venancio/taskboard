package objective.taskboard.it.basecluster;

import static objective.taskboard.it.AbstractIntegrationTest.getAppBaseUrl;
import static objective.taskboard.it.components.ClusterComponent.CLUSTER_TAG;
import static objective.taskboard.it.components.SnackBarComponent.SNACK_BAR_TAG;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.WebDriver;

import objective.taskboard.it.AbstractAppUiFragment;
import objective.taskboard.it.components.ButtonComponent;
import objective.taskboard.it.components.ClusterComponent;
import objective.taskboard.it.components.ClusterRecalculateModalComponent;
import objective.taskboard.it.components.InputComponent;
import objective.taskboard.it.components.SnackBarComponent;
import objective.taskboard.it.components.TextComponent;
import objective.taskboard.it.components.guards.LeaveConfirmationGuard;

public class BaseClusterPage extends AbstractAppUiFragment {

    private static final String PAGE_TAG = "tb-base-cluster";

    private ButtonComponent backToSearchButton;
    private ButtonComponent saveButton;
    private ButtonComponent recalculate;
    private ClusterComponent cluster;
    private SnackBarComponent snackbar;
    private InputComponent name;
    private TextComponent pageTitle;

    public BaseClusterPage(WebDriver webDriver) {
        super(webDriver);
        initElements(webDriver, this);

        this.pageTitle = new TextComponent(webDriver, cssSelector(PAGE_TAG + " #tb-page-title"));
        this.recalculate = new ButtonComponent(webDriver, cssSelector(PAGE_TAG + " obj-toolbar button"));
        this.cluster = new ClusterComponent(webDriver, cssSelector(PAGE_TAG + " " + CLUSTER_TAG));
        this.snackbar = new SnackBarComponent(webDriver, cssSelector(SNACK_BAR_TAG + "#tb-base-cluster-snackbar"));
        this.saveButton = new ButtonComponent(webDriver, cssSelector("button#tb-cluster-save"));
        this.backToSearchButton = new ButtonComponent(webDriver, cssSelector("button#tb-cluster-back-to-project"));
        this.name = new InputComponent(webDriver, cssSelector(PAGE_TAG + " input[name=baseClusterName]"));

        assertPageIsOpen();
    }

    public static String getCreatePageUrl() {
        return getAppBaseUrl() + "base-cluster/new";
    }

    public static String getEditionPageUrl(final int clusterId) {
        return getAppBaseUrl() + "base-cluster/" + clusterId;
    }

    public ClusterRecalculateModalComponent openRecalculate() {
        recalculate.click();
        return new ClusterRecalculateModalComponent(webDriver);
    }

    public BaseClusterPage assertName(final String expectedName) {
        name.assertValue(expectedName);
        return this;
    }

    public BaseClusterPage setName(final String newName) {
        name.setValue(newName);
        return this;
    }

    public BaseClusterPage setEffort(final String issueType, final String size, final String effort) {
        cluster.setEffort(issueType, size, effort);
        return this;
    }

    public BaseClusterPage assertEffort(final String issueType, final String size, final String expectedEffort) {
        cluster.assertEffort(issueType, size, expectedEffort);
        return this;
    }

    public BaseClusterPage setCycle(final String issueType, final String size, final String cycle) {
        cluster.setCycle(issueType, size, cycle);
        return this;
    }

    public BaseClusterPage assertCycle(final String issueType, final String size, final String expectedCycle) {
        cluster.assertCycle(issueType, size, expectedCycle);
        return this;
    }

    public String getNewCycleValue(String issueType, String size) {
        return cluster.getCycleValue(issueType, size);
    }

    public BaseClusterPage selectNewCycleValue(String issueType) {
        cluster.selectNewCycleValue(issueType);
        return this;
    }

    public BaseClusterPage assertCurrentValueNewValueIsShown(String issueType, String tsize) {
        cluster.assertCurrentValueNewValueIsShown(issueType, tsize);
        return this;
    }

    public BaseClusterPage assertSaveButtonEnabled() {
        saveButton.assertIsEnabled();
        return this;
    }

    public BaseClusterPage assertSaveButtonDisabled() {
        saveButton.assertIsDisabled();
        return this;
    }

    public BaseClusterPage save() {
        saveButton.click();
        return this;
    }

    public BaseClusterPage assertErrorMessageIsOpen() {
        snackbar.waitTitleToBe("Error");
        snackbar.waitDescriptionToBe("Please review the form");
        return this;
    }

    public BaseClusterPage assertSuccessMessageIsOpen() {
        snackbar.waitTitleToBe("Success");
        snackbar.waitDescriptionToBe("Base cluster saved");
        return this;
    }

    public BaseClusterSearchPage backToSearchPage() {
        backToSearchButton.click();
        return new BaseClusterSearchPage(webDriver);
    }

    public BaseClusterPage refreshPage() {
        webDriver.navigate().refresh();
        assertPageIsOpen();
        return this;
    }

    public BaseClusterSearchPage backToSearchPageConfirmingLoseChanges() {
        backToSearchButton.click();
        LeaveConfirmationGuard.leave(webDriver);
        return new BaseClusterSearchPage(webDriver);
    }

    private BaseClusterPage assertPageIsOpen() {
        pageTitle.assertText("Base Cluster");
        waitPageLoaderBeHide();
        return this;
    }

}
