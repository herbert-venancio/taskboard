package objective.taskboard.it.config.project;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;

import objective.taskboard.it.components.ButtonComponent;
import objective.taskboard.it.components.ModalComponent;

public class ProjectClusterRecalculateModal {

    private final ButtonComponent openButton;
    private final ModalComponent modal;
    private final ButtonComponent recalculateButton;
    private final ButtonComponent cancelButton;
    private final ClusterAlgorithmForm clusterAlgorithmForm;

    public ProjectClusterRecalculateModal(WebDriver webDriver) {
        By toolbarSelector = By.cssSelector("tb-toolbar");
        openButton = new ButtonComponent(webDriver, new ByChained(toolbarSelector, By.cssSelector("button")));
        By dialogSelector = By.cssSelector("tb-modal");
        modal = new ModalComponent(webDriver, dialogSelector);
        recalculateButton = new ButtonComponent(webDriver, new ByChained(dialogSelector, By.xpath("//button[text() = 'Recalculate']")));
        cancelButton = new ButtonComponent(webDriver, new ByChained(dialogSelector, By.xpath("//button[text() = 'Cancel']")));
        clusterAlgorithmForm = new ClusterAlgorithmForm(webDriver, new ByChained(dialogSelector, By.cssSelector("tb-cluster-algorithm")));
    }

    public ProjectClusterRecalculateModal setStartDate(String date) {
        clusterAlgorithmForm.setStartDate(date);
        return this;
    }

    public ProjectClusterRecalculateModal setEndDate(String date) {
        clusterAlgorithmForm.setEndDate(date);
        return this;
    }

    public ProjectClusterRecalculateModal clickRecalculate() {
        recalculateButton.click();
        return this;
    }

    public ProjectClusterRecalculateModal clickCancel() {
        cancelButton.click();
        return this;
    }

    public ProjectClusterRecalculateModal clickClose() {
        modal.close();
        return this;
    }

    public void waitAutoClose() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ProjectClusterRecalculateModal open() {
        openButton.click();
        return this;
    }
}
