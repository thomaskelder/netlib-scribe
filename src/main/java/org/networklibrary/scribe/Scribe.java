package org.networklibrary.scribe;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.networklibrary.core.config.ConfigManager;

public class Scribe {

	protected static final Logger log = Logger.getLogger(Scribe.class.getName());

	private String db = null;
	private String type = null;
	private String query = null;
	private String queryType;
	private List<String> outputFiles = null;
	private ConfigManager confMgr = null;

	public Scribe(String db, String type, String query,
			String queryType, List<String> outputFiles, ConfigManager confMgr) {
		this.db = db;
		this.type = type;
		this.query = query;
		this.queryType = queryType;
		this.outputFiles = outputFiles;
		this.confMgr = confMgr;
	}

	public void execute() {

		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(db);


		if(query != null && !query.isEmpty()){
			if(queryType.equals("ids")){
				List<String> ids = Arrays.asList(query.split(",",-1));

				if(ids.size() > 0){
					for(String id : ids){
						Node n = queryId(id);

						crawlGraph(n,outputFiles.get(0));
					}
				}

			}
		}

		// ignore type
		// ignore query for now
	}

	protected Node queryId(String id) {
		return null;
	}

	protected void crawlGraph(Node n, String string) {

	}

}
