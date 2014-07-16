package org.networklibrary.scribe;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

/*
 * adapted from https://github.com/thomaskelder/network-builder/blob/master/src/org/edgeleap/networks/graph/XGMMLWriter.java
 * authored by Thomas Kelder
 *
 */
public class XGMMLWriter implements GraphWriter {
	protected static final Logger log = Logger.getLogger(XGMMLWriter.class.getName());
	final static String NS = "http://www.cs.rpi.edu/XGMML";

	protected Document doc = null;
	protected Element root = null;
	protected PrintWriter writer = null;

	public XGMMLWriter(String graphLabel) {
		doc = new Document();
		root = new Element("graph", NS);
		doc.setRootElement(root);

		root.setAttribute("id", "" + System.currentTimeMillis());
		
	}

	public boolean write() {

		if(writer != null){
			XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
			Format f = xmlcode.getFormat();
			f.setEncoding("UTF-8");
			f.setTextMode(Format.TextMode.PRESERVE);
			xmlcode.setFormat(f);
			
			try {
			xmlcode.output(doc, writer);
			return true;
			} catch (IOException e){
				log.severe("writing failed! " + e.getMessage());
				return false;
			}
		}
		
		return false;
	}

	public boolean addNode(Node n) {
		Element e = new Element("node");

		String id = new Long(n.getId()).toString();
		e.setAttribute("id", id);
		e.setAttribute("label", id);


		addAttribute(n, e);

		root.addContent(e);
		return true;
	}

	public boolean addEdge(Relationship r) {
		Element e = new Element("edge");

		String id = new Long(r.getId()).toString();
		e.setAttribute("id", id);
		e.setAttribute("label", id);
		e.setAttribute("source", new Long(r.getStartNode().getId()).toString());
		e.setAttribute("target", new Long(r.getEndNode().getId()).toString());

		Element ie = new Element("att");
		ie.setAttribute("label", "interaction");
		ie.setAttribute("name", "interaction");
		ie.setAttribute("value", r.getType().name());
		ie.setAttribute("type", "string");
		e.addContent(ie);


		addAttribute(r, e);

		root.addContent(e);
		return true;
	}

	protected void addAttribute(PropertyContainer n, Element elm){
		for(String key : n.getPropertyKeys()){

			Object value = n.getProperty(key);
			if(value == null) continue;

			Element e = new Element("att");
			e.setAttribute("label", key);
			e.setAttribute("name", key);


			String valueVal = value.toString();
			String typeVal = "string";

			if(value instanceof Array || value.getClass().isArray()){
				typeVal = "string";
				valueVal = StringUtils.join((Object[])value,"|");
			}

			if(value instanceof Number){
				typeVal = "real";
			}

			//			System.out.println(value.getClass().getName() + "\t" + value.toString() + "\t" + typeVal + "\t" + valueVal);

			e.setAttribute("type", typeVal);
			e.setAttribute("value", valueVal);

			elm.addContent(e);
		}
	}

	@Override
	public void setWriter(PrintWriter writer) {
		this.writer = writer;
	}

	@Override
	public void setGraphLabel(String graphLabel) {
		root.setAttribute("label", graphLabel);
		
	}

}