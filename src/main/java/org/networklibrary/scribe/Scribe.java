package org.networklibrary.scribe;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.networklibrary.core.config.ConfigManager;

public class Scribe {

	protected static final Logger log = Logger.getLogger(Scribe.class.getName());

	private final static String MATCH = "matchid";

	private String type = null;
	private String query = null;
	private String queryType;
	private Map<String, String> extraParams = null; 
	private List<String> outputFiles = null;
	private ConfigManager confMgr = null;
	private String db = null;

	private GraphDatabaseService graph = null;
	private Index<Node> matchableIndex = null;

	public Scribe(String db, String type, String query,
			String queryType, List<String> extras, List<String> outputFiles, ConfigManager confMgr) {

		this.type = type;
		this.query = query;
		this.queryType = queryType;
		this.outputFiles = outputFiles;
		this.confMgr = confMgr;
		this.db = db;

		graph = new GraphDatabaseFactory().newEmbeddedDatabase(db);
		try (Transaction tx = graph.beginTx()){
			matchableIndex = graph.index().forNodes("matchable");
		}
		checkExtraParams(extras);
	}

	public void execute() throws IOException {

//		GraphWriter writer = new XGMMLWriter();
		GraphWriter writer = new DirectXGMMLWriter();
		writer.setWriter(new PrintWriter(outputFiles.get(0)));
		writer.setGraphLabel(db);

		if(query != null && !query.isEmpty()){
			if(queryType.equals("linkedneighbour")){
				List<String> ids = Arrays.asList(query.split(",",-1));

				Set<Node> startNodes = new HashSet<Node>();

				if(ids.size() > 0){
					for(String id : ids){
						startNodes.addAll(queryId(id));
					}
				}

				crawlGraph(startNodes,writer);
				writer.write();
			}

		}

		graph.shutdown();
		// ignore type
		// ignore query for now
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
		int maxDepth = 2;
		String depthParam = getExtraParameter("depth");

		if(depthParam != null && !depthParam.isEmpty()){
			maxDepth = Integer.valueOf(depthParam);
		}

		try (Transaction tx = graph.beginTx()){
			Set<Node> resultNodes = NetworkUtils.dfs(startNodes, maxDepth);

			log.info("dealing with " + resultNodes.size() + " nodes");
			
			for(Node n : resultNodes){
				writer.addNode(n);

				for(Relationship r : n.getRelationships(Direction.OUTGOING)){
					if(resultNodes.contains(r.getOtherNode(n))){
						writer.addEdge(r);
					}
				}
			}
		}
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

	protected boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}

}
