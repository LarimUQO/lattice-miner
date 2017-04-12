package fca.core.context.binary;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class RechercheXml {

public void recherche(){
	String str = "";
	ArrayList<String> RulesList = new ArrayList<String>();
	ArrayList<String> attribut = new ArrayList<String>();

	  try {
	  File file = new File("C:\\test.xml");
	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  DocumentBuilder db = dbf.newDocumentBuilder();
	  Document doc = db.parse(file);
	  doc.getDocumentElement().normalize();
	  NodeList nodeLst = doc.getElementsByTagName("rule");

	  for (int s = 0; s < nodeLst.getLength(); s++) {

	    Node fstNode = nodeLst.item(s);
	    
	    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
	  
	           Element fstElmnt = (Element) fstNode;
	           
	      NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("premise");
	      Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
	      NodeList fstNm = fstNmElmnt.getChildNodes();
	      String f = (String) fstNm.item(0).getNodeValue();
	      if (!(attribut.contains(f)))
	          attribut.add(f);
	      NodeList scdNmElmntLst = fstElmnt.getElementsByTagName("consequence");
	      Element scdNmElmnt = (Element) scdNmElmntLst.item(0);
	      NodeList scdNm = scdNmElmnt.getChildNodes();
	      String f1 = (String) scdNm.item(0).getNodeValue();
	      if (!(attribut.contains(f1)))
	          attribut.add(f1);
	      NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("support");
	      Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
	      NodeList lstNm = lstNmElmnt.getChildNodes();

	      str = ((Node) fstNm.item(0)).getNodeValue() + "->" + ((Node) scdNm.item(0)).getNodeValue()+"["+((Node) lstNm.item(0)).getNodeValue()+"]" ;
	    
	      RulesList.add(str);
	  
	    }
	    
	 
	     }
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
}

 }
