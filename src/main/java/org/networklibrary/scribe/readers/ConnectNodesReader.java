package org.networklibrary.scribe.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import org.networklibrary.scribe.NetworkUtils;
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
				ids = readFromFile(f);
				log.info("query id provided via a file: " + f.getAbsolutePath());
			} else {
				ids = Arrays.asList(query.split(",",-1)); 
			}
			
			log.info("query contains " + ids.size() + " ids (+/-1) if header is present");

			Set<Node> startNodes = new HashSet<Node>();

			if(ids.size() > 0){
				for(String id : ids){
					Set<Node> nodes = queryId(id);
					if(nodes != null && nodes.size() > 0){
						startNodes.addAll(nodes);
					}
				}
			}

			crawlGraph(startNodes,writer);
		}
		
	}

	protected List<String> readFromFile(File f) {
		List<String> ids = null;
		try {
			BufferedReader r = new BufferedReader(new FileReader(f));
			ids = new ArrayList<String>();
		
			while(r.ready()){
				String line = r.readLine();
				ids.add(line.trim());
			}
			r.close();
			
		} catch (IOException e) {
			log.severe("failed to read ids from file " + f.getAbsolutePath() + ": " + e.getMessage());
			return null;
		}
		return ids;
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
		
//		int maxDepth = 2;
//		String depthParam = getExtraParameter("depth");
//
//		if(depthParam != null && !depthParam.isEmpty()){
//			maxDepth = Integer.valueOf(depthParam);
//		}
//
//		try (Transaction tx = graph.beginTx()){
//			Set<Node> resultNodes = NetworkUtils.dfs(startNodes, maxDepth);
//
//			log.info("dealing with " + resultNodes.size() + " nodes");
//
//			for(Node n : resultNodes){
//				writer.addNode(n);
//
//				for(Relationship r : n.getRelationships(Direction.OUTGOING)){
//					if(resultNodes.contains(r.getOtherNode(n))){
//						writer.addEdge(r);
//					}
//				}
//			}
//		}
	}



	protected boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
}
