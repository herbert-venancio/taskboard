package objective.taskboard.it;

import org.openqa.selenium.By;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PageWait {
	public static WebDriverWait wait(WebDriver driver) {
		return new WebDriverWait(driver, 30);
	}

	public static <V> void waitUntilElementExists(WebDriver webDriver, By by) {
		PageWait.wait(webDriver).until((w) -> {
			try {
				webDriver.findElement(by);
			} catch (Exception e) {
				return false;
			}
			return true;
		});
	}
}
