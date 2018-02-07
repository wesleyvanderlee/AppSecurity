package com.bunq.learner.alphabet;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.bunq.util.Property;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * XMLParser class that parses the given XML/PLIST files that contain the
 * numerous elements available in the bunq application's views.
 * 
 * @author michel
 *
 */
public class XMLParser {

	private Document dom;
	private String rootTag;
	private String classType;

	/**
	 * Creates a dom tree of the given xml file.
	 * 
	 * @Constructor creates the dom tree from the given file
	 * @param file
	 *            the file you want to analyze
	 * @param rootTag
	 *            the rootTag in the form of a String. The root tag will be used
	 *            to get all the nodes.
	 * @param classType
	 *            the name of the class that will be used to parse individual
	 *            nodes
	 * @throws FileNotFoundException
	 *             when the file is not a file or does not exist
	 */
	public XMLParser(File file, String rootTag, String classType) throws FileNotFoundException {
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException("The specified path doesn't point to a valid file");
		}
		this.rootTag = rootTag;
		this.classType = classType;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(file);
		} catch (ParserConfigurationException e) {
			System.err.println("Cannot get document builder: " + e.getMessage());
		} catch (SAXException e) {
			System.err.println("Cannot parse document: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error opening file: " + e.getMessage());
		}
	}

	/**
	 * Parses all the elements that can be used to trigger events. Returns a
	 * list of words that both the learner as well as the teaching application
	 * can understand.
	 * 
	 * @return a Set of strings that are words of the alphabet
	 * @throws Exception
	 *             when the reflection code fails. Because of the many
	 *             exceptions this can trigger, all are catched in one Exception
	 *             which than uses the message of the cause as (part of) it's
	 *             own message so that can be seen what the actual error was
	 *             without having to catch all possibilities.
	 */
	public Set<String> parseDocument() throws Exception {
		if (dom == null) {
			throw new DOMException((short) 1, "DOM is either null or has an invalid format");
		}
		Property props = Property.getInstance();
		String appPackage = props.get("appPackage");
		Set<String> words = new HashSet<String>();
		Element root = dom.getDocumentElement();
		NodeList nl = root.getElementsByTagName(rootTag);

		for (int i = 0; i < nl.getLength(); i++) {
			UINode uinode = createUINode((Element) nl.item(i));
			System.out.println(uinode.isInteractive() + "  -  " + uinode.toAlphabetWord().contains(appPackage) + " "
					+ uinode.toAlphabetWord());

			if (uinode.isInteractive() && uinode.toAlphabetWord().contains(appPackage)) {

				words.add(uinode.toAlphabetWord());
			}
		}
		return words;
	}

	/**
	 * Creates an UINode from the given element.
	 * 
	 * @param el
	 *            the element of which this method creates an UINode instance.
	 * @return a UINode (which is either a UINodeAndroid or UINodeiOS currently,
	 *         but this is masked by the interface)
	 * @throws Exception
	 *             when the creation failed due to an error in the java
	 *             reflection method used
	 */
	private UINode createUINode(Element el) throws Exception {
		try {
			Constructor<?> parserConstructor = Class.forName(classType).getConstructor(Element.class);
			return (UINode) parserConstructor.newInstance(el);
		} catch (Exception e) {
			throw new Exception(
					"Error while instantiating " + classType + ", " + "thrown by: " + e.getCause().getMessage());
		}
	}
}
