package learn;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLBuilderTest2 {
	
	private static Log log = LogFactory.getLog(XMLBuilderTest2.class);

	/*
	private static  void listAttrs(NamedNodeMap nnm){
try {
		int len = nnm.getLength();
	for (int i = 0; i < len; i++){
		Node item = nnm.item(i);
		System.out.println(item.getNodeName() + " " + item.getNodeValue());
	}} catch (NullPointerException npe) {
		System.out.println("Empty!");
	}
	}
	*/
	public static String xmlVersion = "1.0";
	public static boolean standalode = false;
	
	
	public static void main(String[] args){
		try {
	
			
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			//NamedNodeMap docAttributes = doc.getAttributes();
			//listAttrs(docAttributes);
			doc.setXmlVersion(xmlVersion);
			doc.setXmlStandalone(standalode);
			
			
			Element rootElement = doc.createElement("company");
			doc.appendChild(rootElement);
			//NamedNodeMap rootElementAttributes = rootElement.getAttributes();
			//listAttrs(rootElementAttributes);
			
			Element staff =  doc.createElement("Staff");
			rootElement.appendChild(staff);
			
			Attr attr = doc.createAttribute("id");
			staff.setAttributeNode(attr);
			
			
			Element firstname = doc.createElement("firstname");
			firstname.appendChild(doc.createTextNode("Maksym"));
			staff.appendChild(firstname);
			
			Element lastname = doc.createElement("lastname");
			lastname.appendChild(doc.createTextNode("Shkolnyi"));
			staff.appendChild(lastname);
			
			Element nickname = doc.createElement("nickname");
			nickname.appendChild(doc.createTextNode("maskimko"));
			staff.appendChild(nickname);
			
			Element salary = doc.createElement("salary");
			salary.appendChild(doc.createTextNode("100000"));
			staff.appendChild(salary);
			
			Element incident = doc.createElement("incident_tag");
			Attr incidentXMLRelationships = doc.createAttribute("relationships_included");
			incidentXMLRelationships.setValue("xml_relationships");
			incident.setAttributeNode(incidentXMLRelationships);
			incident.setAttribute("type", "xml_type");
			incident.setAttribute("version", "xml_version");
			incident.setAttribute("xmlns", "xml_namespace");
			rootElement.appendChild(incident);
			
			
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			Transformer transformer = transformerFactory.newTransformer();
			
			DOMSource source = new DOMSource(doc);
			//StreamResult result = new StreamResult(new File("d:\\file.xml"));
			//StreamResult result = new StreamResult(System.out);
			StringWriter writer = new StringWriter();
			StreamResult  result = new StreamResult(writer);
			transformer.transform(source, result);
			
			System.out.println("\nFile saved!");
			System.out.println(writer.toString());
			log.debug( "It works! " + log.toString());
	
		} catch (ParserConfigurationException pce){
			pce.printStackTrace();
		} catch (TransformerException tfe){
			tfe.printStackTrace();
		}
	}
}
