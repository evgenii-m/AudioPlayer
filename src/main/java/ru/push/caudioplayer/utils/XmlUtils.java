package ru.push.caudioplayer.utils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
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

	public static <T> T unmarshalDocumnet(String xmlDocument, String jaxbContextPath) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(jaxbContextPath);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		@SuppressWarnings("unchecked")
		T t = (T) unmarshaller.unmarshal(new StringReader(xmlDocument));
		return t;
	}

	public static <T> void marshalDocument(T document, BufferedWriter stream, String jaxbContextPath) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(jaxbContextPath);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.marshal(document, stream);
	}
}
