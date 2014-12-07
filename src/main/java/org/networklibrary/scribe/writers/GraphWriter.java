package org.networklibrary.scribe.writers;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public interface GraphWriter {

	public void setWriter(String filename);
	public void setGraphLabel(String graphLabel);
	
	public boolean addNode(Node n);
	public boolean addEdge(Relationship r);
	public boolean finishUp();
	
}