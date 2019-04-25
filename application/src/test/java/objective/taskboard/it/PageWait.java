package objective.taskboard.it;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PageWait {

	public static WebDriverWait wait(WebDriver driver) {
		return new WebDriverWait(driver, 30);
	}

	private final WebDriver webDriver;

	public PageWait(WebDriver webDriver) {
		this.webDriver = webDriver;
	}

	public WebElement allConditions(WebElement element, WebElementCondition[] conditions) {
		wait(webDriver)
				.until(webDriver -> Arrays.stream(conditions)
						.allMatch(condition -> condition.test(element)));
		return element;
	}

	public WebElement allConditions(By selector, WebElementCondition[] conditions) {
		return wait(webDriver)
				.ignoring(StaleElementReferenceException.class)
				.until(webDriver -> {
					List<WebElement> elements = webDriver.findElements(selector);
					if(elements.isEmpty())
						return null;

					WebElement element = elements.get(0);

					if(!Arrays.stream(conditions).allMatch(c -> c.test(element)))
						return null;

					return element;
				});
	}

	public List<WebElement> allConditions(By selector, WebElementsCondition[] conditions) {
		return wait(webDriver)
				.ignoring(StaleElementReferenceException.class)
				.until(webDriver -> {
					List<WebElement> elements = webDriver.findElements(selector);

					if(!Arrays.stream(conditions).allMatch(c -> c.test(elements)))
						return null;

					return elements;
				});
	}

	public WebElement allConditions(WebElement parent, By selector, WebElementCondition[] conditions) {
		return wait(webDriver)
				.until(webDriver -> {
					List<WebElement> elements = parent.findElements(selector);

					if(elements.isEmpty())
						return null;

					WebElement element = elements.get(0);

					if(!Arrays.stream(conditions).allMatch(c -> c.test(element)))
						return null;

					return element;
				});
	}

	public List<WebElement> allConditions(WebElement parent, By selector, WebElementsCondition[] conditions) {
		return wait(webDriver)
				.ignoring(StaleElementReferenceException.class)
				.until(webDriver -> {
					List<WebElement> elements = parent.findElements(selector);

					if (!Arrays.stream(conditions).allMatch(c -> c.test(elements)))
						return null;

					return elements;
				});
	}

	public interface WebElementCondition extends Predicate<WebElement> {
		@Override
		default WebElementCondition negate() {
			return element -> !test(element);
		}
	}
	public interface WebElementsCondition extends Predicate<List<WebElement>> {

		@Override
		default Predicate<List<WebElement>> negate() {
			return element -> !test(element);
		}
	}

	public static WebElementCondition elementExist() {
		return Objects::nonNull;
	}

	public static WebElementCondition elementIsVisible() {
		return WebElement::isDisplayed;
	}

	public static WebElementCondition elementIsClickable() {
		return WebElement::isEnabled;
	}

	public static WebElementCondition elementClicked() {
		return element -> {
			try {
				element.click();
				return true;
			} catch(WebDriverException ex) {
				if (ex.getMessage().matches("(?sm).*Element .* is not clickable at point .* because another element .* obscures it.*"))
					return false;
				throw ex;
			}
		};
	}

	public static WebElementCondition elementTextIs(String expectedText) {
		return element -> expectedText.equals(element.getText());
	}

	public static WebElementCondition elementTextContains(String expectedText) {
		return element -> element.getText().contains(expectedText);
	}

	public static WebElementCondition attributeToBe(String attribute, String expected) {
		return element -> getAttributeValue(element, attribute)
				.map(actual -> actual.equals(expected))
				.orElse(false);
	}

	public static WebElementCondition attributeContains(String attribute, String expected) {
		return element -> getAttributeValue(element, attribute)
				.map(actual -> actual.contains(expected))
				.orElse(false);
	}

	public static WebElementsCondition noneExists() {
		return elements -> elements.size() == 0;
	}

	public static WebElementsCondition atLeastOneElementExists() {
		return elements -> elements.size() > 0;
	}

	private static Optional<String> getAttributeValue(WebElement element, String attribute) {
		String currentValue = element.getAttribute(attribute);
		if (StringUtils.isNotEmpty(currentValue))
			return Optional.of(currentValue);
		return Optional.ofNullable(element.getCssValue(attribute));
	}
}
