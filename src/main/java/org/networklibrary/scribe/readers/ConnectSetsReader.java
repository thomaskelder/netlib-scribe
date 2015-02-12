package org.networklibrary.scribe.readers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.networklibrary.scribe.helpers.DataFromFiles;
import org.networklibrary.scribe.writers.GraphWriter;

public class ConnectSetsReader extends ConnectNodesReader{

	public ConnectSetsReader(GraphDatabaseService graph,
			GraphWriter graphWriter, List<String> extras) {
		super(graph,graphWriter,extras);
	}

	public void execute(String query) {
		
		List<String> set1 = null;
		List<String> set2 = null;
		
		if(query != null && !query.isEmpty()){
			set1 = getIdSet(query);
		}
		
		String extraSet = getExtraParameter("set");
		if(extraSet != null){
			set2 = getIdSet(extraSet);
		}
		
		if(set1 != null && set2 != null){
			
			Set<Node> set1Nodes = idsToNodes(set1);
			Set<Node> set2Nodes = idsToNodes(set2);
			
			crawlGraph(set1Nodes, set2Nodes);
		}
	}
	
	protected List<String> getIdSet(String query){
		List<String> set = null;
		
		File f = new File(query);
	
		if(f.exists()){
			try {
				set = DataFromFiles.readFromFile(f);
			} catch (IOException e) {
				log.severe("failed to read ids from file " + f.getAbsolutePath() + ": " + e.getMessage());
				set = null;
			}
			log.info("query id provided via a file: " + f.getAbsolutePath());
		} else {
			// should allow for spaces and ; as well.
			set = Arrays.asList(query.split(",",-1)); 
		}
		
		log.info("query contains " + set.size() + " ids (+/-1) if header is present");
		return set;
	}
	
	protected void crawlGraph(Set<Node> set1, Set<Node> set2){
		if(set1 != null && set2 != null){
			for(Node n1 : set1){
				for(Relationship e : n1.getRelationships(Direction.OUTGOING)){
					//
				}
			}
		}
		
	}

	
}
