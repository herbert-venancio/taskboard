package objective.taskboard.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LaneFragment extends AbstractUiFragment {
	WebElement laneRoot;

	private LaneFragment(WebDriver driver, String laneName) {
		super(driver);
		laneRoot = getElementWhenItExists(laneSelector(laneName));
	}

	public BoardStepFragment boardStep(String stepName) {
		WebElement webStepElement = laneRoot.findElement(By.cssSelector("[data-step-name='" + stepName + "']"));
		return new BoardStepFragment(webDriver, stepName, webStepElement);
	}

	public static LaneFragment laneName(WebDriver webDriver, String laneName) {
		return new LaneFragment(webDriver, laneName);
	}

	public static By laneSelector(String laneName) {
		return By.cssSelector("[data-lane-name='" + laneName + "']");
	}

}
