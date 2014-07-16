package org.networklibrary.scribe;

import java.io.PrintWriter;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public interface GraphWriter {

	public void setWriter(PrintWriter writer);
	public void setGraphLabel(String graphLabel);
	
	public boolean addNode(Node n);
	public boolean addEdge(Relationship r);
	public boolean write();
	
}