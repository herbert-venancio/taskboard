package objective.taskboard.utils;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by herbert on 30/06/17.
 */
public class XmlUtilsTest {

    @Test
    public void xpathToAttributes() throws URISyntaxException {
        File xmlFile = new File(XmlUtils.class.getResource("file.xml").toURI());
        String locator = "//element/@attribute";

        NodeList nodeList = XmlUtils.xpath(xmlFile, locator);
        assertThat(nodeList.getLength(), is(2));
        assertThat(nodeList.item(0).getNodeValue(), is("value0"));
        assertThat(nodeList.item(1).getNodeValue(), is("value1"));
    }

    @Test
    public void xpathToNode() throws URISyntaxException {
        File xmlFile = new File(XmlUtils.class.getResource("file.xml").toURI());
        String locator = "//element[@attribute='value0']";

        NodeList nodeList = XmlUtils.xpath(xmlFile, locator);
        assertThat(nodeList.getLength(), greaterThan(0));
        assertThat(nodeList.item(0).getAttributes().getNamedItem("attribute").getNodeValue(), is("value0"));
    }

    @Test
    public void formatXml() throws URISyntaxException, IOException, TransformerException {
        File outputXmlFile = Files.createTempFile("formatted", "xml").toFile();
        try {
            File inputXmlFile = new File(XmlUtils.class.getResource("unformattedFile.xml").toURI());
            File expectedResult = new File(XmlUtils.class.getResource("file.xml").toURI());

            XmlUtils.format(inputXmlFile, outputXmlFile);

            assertEquals("The files differ!",
                    FileUtils.readFileToString(expectedResult, "utf-8"),
                    FileUtils.readFileToString(outputXmlFile, "utf-8")
            );
        } finally {
            outputXmlFile.delete();
        }
    }

    @Test
    public void asStringAttribute() throws URISyntaxException, TransformerException {
        File xmlFile = new File(XmlUtils.class.getResource("file.xml").toURI());
        String locator = "//element[@attribute='value0']/@attribute";

        assertThat(
                XmlUtils.asString(XmlUtils.xpath(xmlFile, locator))
                , is("value0"));
    }

    @Test
    public void asStringAttributes() throws URISyntaxException, TransformerException {
        File xmlFile = new File(XmlUtils.class.getResource("file.xml").toURI());
        String locator = "//element/@attribute";

        assertThat(
                XmlUtils.asString(XmlUtils.xpath(xmlFile, locator))
                , is("value0\nvalue1"));
    }

    @Test
    public void asStringNode() throws URISyntaxException, TransformerException {
        File xmlFile = new File(XmlUtils.class.getResource("file.xml").toURI());
        String locator = "//element[@attribute='value0']";

        assertThat(
                XmlUtils.asString(XmlUtils.xpath(xmlFile, locator))
                , is("<element attribute=\"value0\"/>"));
    }

    @Test
    public void asStringNodes() throws URISyntaxException, TransformerException {
        File xmlFile = new File(XmlUtils.class.getResource("file.xml").toURI());
        String locator = "//element";

        assertThat(
                XmlUtils.asString(XmlUtils.xpath(xmlFile, locator))
                , is("<element attribute=\"value0\"/>\n<element attribute=\"value1\">text-content</element>"));
    }

    @Test
    public void asStringText() throws URISyntaxException, TransformerException {
        File xmlFile = new File(XmlUtils.class.getResource("file.xml").toURI());
        String locator = "//element[@attribute='value1']/text()";

        assertThat(
                XmlUtils.asString(XmlUtils.xpath(xmlFile, locator))
                , is("text-content"));
    }
}
