package ru.push.caudioplayer.utils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class XmlUtils {

	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

	public static Document convertStringToXmlDocument(String content) throws IOException,
			ParserConfigurationException, SAXException {

		try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
			return documentBuilderFactory.newDocumentBuilder().parse(is);
		}
	}

	public static String convertXmlDocumentToSting(Document document) throws TransformerException {
		StringWriter sw = new StringWriter();
		// Transformer not thread safe, so initialization left in method
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "false");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.transform(new DOMSource(document), new StreamResult(sw));
		return sw.toString();
	}
}
