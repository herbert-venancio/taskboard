package objective.taskboard.it.config.project;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;

import objective.taskboard.it.components.ButtonComponent;
import objective.taskboard.it.components.ModalComponent;

public class ProjectClusterRecalculateModal {

    private final ButtonComponent openButton;
    private final ModalComponent modal;
    private final ButtonComponent recalculateButton;
    private final ClusterAlgorithmForm clusterAlgorithmForm;

    public ProjectClusterRecalculateModal(WebDriver webDriver) {
        By toolbarSelector = By.cssSelector("obj-toolbar");
        openButton = new ButtonComponent(webDriver, new ByChained(toolbarSelector, By.cssSelector("button")));
        By modalSelector = By.cssSelector("obj-modal");
        modal = new ModalComponent(webDriver, modalSelector);
        recalculateButton = new ButtonComponent(webDriver, new ByChained(modalSelector, By.xpath("//button[text() = 'Recalculate']")));
        clusterAlgorithmForm = new ClusterAlgorithmForm(webDriver, new ByChained(modalSelector, By.cssSelector("tb-cluster-algorithm")));
    }

    public ProjectClusterRecalculateModal setStartDate(String date) {
        clusterAlgorithmForm.startDateInput.setValue(date);
        return this;
    }

    public ProjectClusterRecalculateModal assertStartDateHasNoError() {
        assertThat(clusterAlgorithmForm.startDateErrorMessages.hasErrorMessages()).isFalse();
        return this;
    }

    public ProjectClusterRecalculateModal assertStartDateHasErrorMessage() {
        assertThat(clusterAlgorithmForm.endDateErrorMessages.hasErrorMessages()).isTrue();
        return this;
    }

    public ProjectClusterRecalculateModal setEndDate(String date) {
        clusterAlgorithmForm.endDateInput.setValue(date);
        return this;
    }

    public ProjectClusterRecalculateModal assertEndDateHasNoError() {
        assertThat(clusterAlgorithmForm.endDateErrorMessages.hasErrorMessages()).isFalse();
        return this;
    }

    public ProjectClusterRecalculateModal assertEndDateHasErrorMessage() {
        assertThat(clusterAlgorithmForm.endDateErrorMessages.hasErrorMessages()).isTrue();
        return this;
    }

    public ProjectClusterRecalculateModal clickRecalculate() {
        recalculateButton.click();
        return this;
    }

    public ProjectClusterRecalculateModal open() {
        openButton.click();
        return assertIsOpen();
    }

    public ProjectClusterRecalculateModal assertIsOpen() {
        modal.assertIsOpen();
        return this;
    }

    public ProjectClusterRecalculateModal assertIsClosed() {
        modal.assertIsClosed();
        return this;
    }
}
