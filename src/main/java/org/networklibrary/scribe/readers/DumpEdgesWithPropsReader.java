package org.networklibrary.scribe.readers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

public class DumpEdgesWithPropsReader {

	private GraphDatabaseService graph;
	private String filename;
	
	
	public DumpEdgesWithPropsReader(GraphDatabaseService graph, String filename) {
		this.graph = graph;
		this.filename = filename;
		
	}

	public void execute() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			Set<String> properties = new HashSet<String>();
			
			try(Transaction tx = graph.beginTx()) {
				//Get all relationship properties
				for(Relationship r : GlobalGraphOperations.at(graph).getAllRelationships()) {
					IteratorUtil.addToCollection(r.getPropertyKeys(), properties);
				}
				
				Map<String, Integer> propertyIndex = new HashMap<String, Integer>();
				int index = 2;
				for(String p : new TreeSet<String>(properties)) {
					propertyIndex.put(p, index++);
				}
				
				//Dump all relationships
				for(Relationship r : GlobalGraphOperations.at(graph).getAllRelationships()) {
					String[] row = new String[properties.size() + 2];
					Arrays.fill(row, "");
					row[0] = "" + r.getStartNode().getId();
					row[1] = "" + r.getEndNode().getId();
					for(String p : r.getPropertyKeys()) {
						row[propertyIndex.get(p)] = "" + r.getProperty(p);
					}
					writer.write(StringUtils.join(row, "\t"));
					writer.newLine();
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
