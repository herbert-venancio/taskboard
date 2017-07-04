package objective.taskboard.utils;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by herbert on 30/06/17.
 */
public class XmlUtils {

    public static NodeList xpath(File xmlFile, String locator) {
        try {
            // create XPath
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(locator);

            // create Document
            Document doc = asDocument(xmlFile);

            // searches Document using XPath
            return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (Exception e) {
            throw new InvalidXPathOperationException(e);
        }
    }

    public static Document asDocument(File xmlFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(xmlFile);
        } catch (Exception e) {
            throw new InvalidXmlException(e);
        }
    }

    public static String asString(NodeList nodeList) throws TransformerException {
        if(nodeList.getLength() > 0) {
            StringWriter writer = new StringWriter();
            for(int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                if (i > 0)
                    writer.append("\n");
                switch (node.getNodeType()) {
                    case Node.ATTRIBUTE_NODE:
                    case Node.TEXT_NODE:
                        writer.append(node.getNodeValue());
                        break;
                    default:
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        transformer.transform(new DOMSource(node), new StreamResult(writer));
                }
            }
            return writer.toString();
        }
        return "";
    }

    public static void format(File inputXmlFile, File outputXmlFile)  {
        try {
            final Document document = asDocument(inputXmlFile);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(0);
            format.setIndenting(true);
            format.setIndent(2);
            format.setOmitXMLDeclaration(true);
            FileWriter out = new FileWriter(outputXmlFile);
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class InvalidXPathOperationException extends RuntimeException {
        private static final String MESSAGE = "Invalid XPath Operation";
        public InvalidXPathOperationException() {
            super(MESSAGE);
        }
        public InvalidXPathOperationException(Exception e) {
            super(MESSAGE, e);
        }
    }

    public static class InvalidXmlException extends RuntimeException {
        private static final String MESSAGE = "Invalid Xml";
        public InvalidXmlException() {
            super(MESSAGE);
        }
        public InvalidXmlException(Exception e) {
            super(MESSAGE, e);
        }
    }
}
