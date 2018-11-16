package de.keawe.keawallet.objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * @author Stephan Richter
 * This program is not intendet to be used on android.
 * It is just delivered with hbci4android, as is provides an xml
 * converter needed to sanitize hbci syntax files for use with android apps.
 *
 */
public class XmlSanitizer {

	InputStream sourceStream;
	File destinationFile;

	/**
	 * Create a new sanitizer object.
	 * @param source the ansolute path and name of the source xml file
	 * @param dest the absolute path and name of the destination xml file
	 * @throws FileNotFoundException if the source file can not be accessed.
	 */
	public XmlSanitizer(InputStream sourceStream, String dest) throws IOException {
		if (sourceStream==null) throw new IOException("Source stream must not be null!");
		this.sourceStream=sourceStream;
		destinationFile=new File(dest);
	}

	/**
	 * this method parses the source xml file and writes the sanitized destination file
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void work() throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();

		dbf.setIgnoringComments(true);
		dbf.setValidating(false);

		DocumentBuilder db=dbf.newDocumentBuilder();
		Document syntax = db.parse(sourceStream);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(syntax);
		StreamResult result = new StreamResult(destinationFile);

		transformer.transform(source, result);

		System.out.println("done.");
	}
}
