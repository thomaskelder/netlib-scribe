package org.networklibrary.scribe.readers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

public class DumpEdgesReader {

	private GraphDatabaseService graph;
	private String filename;
	
	
	public DumpEdgesReader(GraphDatabaseService graph, String filename) {
		this.graph = graph;
		this.filename = filename;
		
	}

	public void execute() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			try(Transaction tx = graph.beginTx()){
				for(Node n : GlobalGraphOperations.at(graph).getAllNodes()){
					for(Relationship r : n.getRelationships(Direction.OUTGOING)){
						writer.write(r.getStartNode().getId() + "\t" + r.getEndNode().getId());
						writer.newLine();
						
					}
				}
				writer.flush();
				
				tx.success();
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
