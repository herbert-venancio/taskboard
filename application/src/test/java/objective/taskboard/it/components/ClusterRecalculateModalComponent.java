package objective.taskboard.it.components;

import static objective.taskboard.it.components.SnackBarComponent.SNACK_BAR_TAG;
import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;

public class ClusterRecalculateModalComponent extends AbstractComponent {

    private static final String CLUSTER_RECALCULATE_MODAL_TAG = "tb-cluster-recalculate-modal";

    private final ModalComponent modal;
    private final ButtonComponent recalculateButton;
    private final ClusterAlgorithmFormComponent clusterAlgorithmForm;

    public ClusterRecalculateModalComponent(WebDriver webDriver) {
        super(webDriver, By.cssSelector(CLUSTER_RECALCULATE_MODAL_TAG));

        By modalSelector = By.cssSelector("obj-modal");
        modal = new ModalComponent(webDriver, modalSelector);
        recalculateButton = new ButtonComponent(webDriver, new ByChained(modalSelector, By.xpath("//button[text() = 'Recalculate']")));
        clusterAlgorithmForm = new ClusterAlgorithmFormComponent(webDriver, new ByChained(modalSelector, By.cssSelector("tb-cluster-algorithm")));
        assertRecalculateIsOpened();
    }

    public ClusterRecalculateModalComponent setStartDate(String date) {
        clusterAlgorithmForm.startDateInput.setValue(date);
        return this;
    }

    public ClusterRecalculateModalComponent assertStartDateHasNoError() {
        assertThat(clusterAlgorithmForm.startDateErrorMessages.hasErrorMessages()).isFalse();
        return this;
    }

    public ClusterRecalculateModalComponent assertStartDateHasError() {
        assertThat(clusterAlgorithmForm.endDateErrorMessages.hasErrorMessages()).isTrue();
        return this;
    }

    public ClusterRecalculateModalComponent setEndDate(String date) {
        clusterAlgorithmForm.endDateInput.setValue(date);
        return this;
    }

    public ClusterRecalculateModalComponent assertEndDateHasNoError() {
        assertThat(clusterAlgorithmForm.endDateErrorMessages.hasErrorMessages()).isFalse();
        return this;
    }

    public ClusterRecalculateModalComponent assertEndDateHasError() {
        assertThat(clusterAlgorithmForm.endDateErrorMessages.hasErrorMessages()).isTrue();
        return this;
    }

    public ClusterRecalculateModalComponent selectProject(String projectName) {
        assertRecalculateIsOpened();
        clusterAlgorithmForm.getProjectCheckbox(projectName).select();
        return this;
    }

    public ClusterRecalculateModalComponent unselectProject(String projectName) {
        assertRecalculateIsOpened();
        clusterAlgorithmForm.getProjectCheckbox(projectName).unselect();
        return this;
    }

    public ClusterRecalculateModalComponent recalculate() {
        recalculateButton.click();
        return this;
    }

    public ClusterRecalculateModalComponent assertRecalculateIsOpened() {
        modal.assertIsOpen();
        return this;
    }

    public ClusterRecalculateModalComponent assertRecalculateIsClosed() {
        modal.assertIsClosed();
        return this;
    }

    public ClusterRecalculateModalComponent assertProjectsHaveError() {
        SnackBarComponent snackBar = new SnackBarComponent(webDriver, By.cssSelector(SNACK_BAR_TAG));
        snackBar.waitTitleToBe("Cluster recalculation error");
        snackBar.waitDescriptionsToBe("You must select at least one project.");
        return this;
    }
}
