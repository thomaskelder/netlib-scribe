package org.networklibrary.scribe;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

public class DirectXGMMLWriter implements GraphWriter {
	protected static final Logger log = Logger.getLogger(DirectXGMMLWriter.class.getName());

	protected PrintWriter writer = null;
	protected XMLOutputter xmlcode = null;
	protected String graphLabel = null;

	public DirectXGMMLWriter(){
	}

	@Override
	public void setWriter(PrintWriter writer) {
		this.writer = writer;

	}
	
	@Override
	public void setGraphLabel(String graphLabel){
		this.graphLabel = graphLabel;
	}

	@Override
	public boolean addNode(Node n) {
		Element e = new Element("node");

		String id = new Long(n.getId()).toString();
		e.setAttribute("id", id);
		e.setAttribute("label", id);


		addAttribute(n, e);

		return writeToFile(e);
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

		return writeToFile(e);
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

	private boolean writeToFile(Element e){
		try{
			if(xmlcode == null)
				prepWriting();

			xmlcode.output(e, writer);
			return true;
		} catch (IOException ex){
			log.severe("failed to write!" + ex.getMessage());
		}
		return false;
	}

	private void prepWriting(){
		xmlcode = new XMLOutputter(Format.getPrettyFormat());
		Format f = xmlcode.getFormat();
		f.setEncoding("UTF-8");
		f.setTextMode(Format.TextMode.PRESERVE);
		xmlcode.setFormat(f);

		writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.print("<graph xmlns=\"http://www.cs.rpi.edu/XGMML\" id=\""+System.currentTimeMillis()+"\" label=\""+graphLabel+"\">");
	}

	@Override
	public boolean write() {

		if(writer != null){
			writer.print("</graph>");
			writer.close();
			return true;
		}

		return false;

	}

}
