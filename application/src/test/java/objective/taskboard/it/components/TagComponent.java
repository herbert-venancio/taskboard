package objective.taskboard.it.components;

import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import objective.taskboard.testUtils.ColorUtils;

public class TagComponent extends AbstractComponent {

    public static final String TAG_TAG = "obj-tag";

    public TagComponent(WebDriver webDriver, By componentSelector) {
        super(webDriver, componentSelector);
    }

    public void waitBe(String text, TagType tagType) {
        waitTextBe(text);
        waitTypeBe(tagType);
    }

    private void waitTextBe(String text) {
        waitTextInElement(textEl(), text);
    }

    private void waitTypeBe(TagType tagType) {
        waitTrue(() -> getType().equals(tagType));
    }

    public String getText() {
        return textEl().getText();
    }

    public TagType getType() {
        String type = tagEl().getAttribute("type");
        String rgbColor = tagEl().getCssValue("background-color");
        String hexColor = ColorUtils.rgbToHex(rgbColor);
        return TagType.from(type, hexColor);
    }

    public String getFormattedValues() {
        return getType().type() +":"+ getText();
    }

    public String getFormattedValues(String before, String after) {
        return before + getFormattedValues() + after;
    }

    private WebElement tagEl() {
        return getChildElementWhenExists(component(), cssSelector(".tag"));
    }

    private WebElement textEl() {
        return getChildElementWhenExists(component(), cssSelector(".text"));
    }

    public static enum TagType {

        ROW_ADDED("row-added", "#15955A"),
        NOT_REGISTERED(null, null);

        private final String type;
        private final String color;

        TagType(String type, String color) {
            this.type = type;
            this.color = color;
        }

        public String type() {
            return this.type;
        }

        public String color() {
            return this.color;
        }

        public static TagType from(String type, String color) {
            for (TagType tagType : values())
                if (type.equals(tagType.type()) && color.toUpperCase().equals(tagType.color().toUpperCase()))
                    return tagType;
            return NOT_REGISTERED;
        }

    }

}
