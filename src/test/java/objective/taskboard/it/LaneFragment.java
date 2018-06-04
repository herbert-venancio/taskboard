package objective.taskboard.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LaneFragment extends AbstractUiFragment {
	WebElement laneRoot;

	public LaneFragment(WebDriver driver, String laneName) {
		super(driver);
		laneRoot = driver.findElement(By.cssSelector("[data-lane-name='" + laneName + "']"));
	}

	public BoardStepFragment boardStep(String stepName) {
		WebElement webStepElement = laneRoot
				.findElement(By.cssSelector("[data-board-step='board-step-" + stepName + "']"));
		;
		return new BoardStepFragment(webDriver, stepName, webStepElement);
	}

	public static LaneFragment laneName(WebDriver webDriver, String laneName) {
		PageWait.waitUntilElementExists(webDriver, By.cssSelector("[data-lane-name='" + laneName + "']"));
		return new LaneFragment(webDriver, laneName);
	}
}
