package org.networklibrary.scribe.readers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.networklibrary.scribe.helpers.DataFromFiles;
import org.networklibrary.scribe.writers.GraphWriter;

public class ConnectNodesReader {

	protected static final Logger log = Logger.getLogger(ConnectNodesReader.class.getName());

	private final static String MATCH = "matchid";

	private GraphWriter writer;

	private Map<String, String> extraParams = null;

	private GraphDatabaseService graph;
	private Index<Node> matchableIndex = null;

	public ConnectNodesReader(GraphDatabaseService graph, GraphWriter writer,List<String> extras){
		this.graph = graph;
		this.writer = writer;

		if(extras != null && extras.size() > 0)
			checkExtraParams(extras);

		try (Transaction tx = graph.beginTx()){
			matchableIndex = graph.index().forNodes("matchable");
		}
	}

	public void execute(String query){
		if(query != null && !query.isEmpty()){

			List<String> ids = null;

			File f = new File(query);
			
			if(f.exists()){
				try {
					ids = DataFromFiles.readFromFile(f);
				} catch (IOException e) {
					log.severe("failed to read ids from file " + f.getAbsolutePath() + ": " + e.getMessage());
					ids = null;
				}
				log.info("query id provided via a file: " + f.getAbsolutePath());
			} else {
				// should allow for spaces and ; as well.
				ids = Arrays.asList(query.split(",",-1)); 
			}
			
			log.info("query contains " + ids.size() + " ids (+/-1) if header is present");

			Set<Node> startNodes = idsToNodes(ids);

			if(startNodes != null)
				crawlGraph(startNodes,writer);
		}
		
	}
	
	protected Set<Node> idsToNodes(List<String> ids){
		Set<Node> res = null;
		
		if(ids != null && ids.size() > 0){
			res = new HashSet<Node>();
			for(String id : ids){
				Set<Node> nodes = queryId(id);
				if(nodes != null && nodes.size() > 0){
					res.addAll(nodes);
				}
			}
		}
		
		return res;
	}

	protected void checkExtraParams(List<String> extras){

		if(extras.size() > 0){
			extraParams = new HashMap<String,String>();
		}

		for(String extra : extras){
			String values[] = extra.split("=",-1);

			extraParams.put(values[0], values[1]);
		}
	}

	protected String getExtraParameter(String name){
		String res = null;
		if(extraParams != null){
			res = extraParams.get(name);
		}

		return res;
	}

	protected Set<Node> queryId(String id) {
		Set<Node> result = null;
		try (Transaction tx = graph.beginTx()) {
			IndexHits<Node> hits = matchableIndex.get(MATCH, id);

			if(hits.size() > 0)
				result = new HashSet<Node>();

			for(Node n : hits){
				result.add(n);
			}
			hits.close();
		}

		return result;
	}

	protected void crawlGraph(Set<Node> startNodes, GraphWriter writer) {
		
		try(Transaction tx = graph.beginTx()){
			
			for(Node n : startNodes){
				writer.addNode(n);
				
				for(Relationship r : n.getRelationships(Direction.OUTGOING)){
					if(startNodes.contains(r.getOtherNode(n))){
						writer.addEdge(r);
					}
				}
			}	
		}
	}

	
	
	protected GraphDatabaseService getGraph() {
		return graph;
	}

	protected Index<Node> getMatchableIndex() {
		return matchableIndex;
	}
	
	protected GraphWriter getWriter() {
		return writer;
	}

	protected boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
}
