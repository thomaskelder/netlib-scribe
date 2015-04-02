package org.networklibrary.scribe;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.networklibrary.core.config.ConfigManager;
import org.networklibrary.scribe.readers.ConnectNodesReader;
import org.networklibrary.scribe.readers.ConnectSetsReader;
import org.networklibrary.scribe.readers.CypherQueryReader;
import org.networklibrary.scribe.readers.DumpEdgesReader;
import org.networklibrary.scribe.readers.DumpEdgesWithPropsReader;
import org.networklibrary.scribe.writers.CypherResultWriter;
import org.networklibrary.scribe.writers.GraphWriter;
import org.networklibrary.scribe.writers.impl.CypherTabWriter;
import org.networklibrary.scribe.writers.impl.CypherXGMMLWriter;
import org.networklibrary.scribe.writers.impl.DirectXGMMLWriter;

public class Scribe {

	protected static final Logger log = Logger.getLogger(Scribe.class.getName());

	private String outType = null;
	private String query = null;
	private String queryType; 
	private List<String> outputFiles = null;
	private ConfigManager confMgr = null;
	private String db = null;
	private List<String> extras;

	private GraphDatabaseService graph = null;
	

	public Scribe(String db, String outType, String query,
			String queryType, List<String> extras, List<String> outputFiles, ConfigManager confMgr) {

		this.outType = outType;
		this.query = query;
		this.queryType = queryType;
		this.outputFiles = outputFiles;
		this.confMgr = confMgr;
		this.db = db;
		this.extras = extras;

		graph = new GraphDatabaseFactory().newEmbeddedDatabase(db);		
	}

	public void execute() throws IOException {

		switch(queryType.toLowerCase()){
		case "cypher":

			CypherResultWriter cypherWriter = decideCypherWriter();

			CypherQueryReader cypherExec = new CypherQueryReader(graph,cypherWriter);
			cypherExec.executeCypher(query);
			
			cypherWriter.finishUp();

			break;
			
		case "connect":
			{
			GraphWriter graphWriter = decideGraphWriter();
			
			ConnectNodesReader connNodesReader = new ConnectNodesReader(graph, graphWriter, extras);
			connNodesReader.execute(query);
			
			graphWriter.finishUp();
			}
			break;
			
		case "connectSets":
			{
			GraphWriter graphWriter = decideGraphWriter();
			
			ConnectSetsReader connectSetsReader = new ConnectSetsReader(graph,graphWriter, extras);
			connectSetsReader.execute(query);
			
			graphWriter.finishUp();
			}
			break;
			
		case "dump_edges":
			
			DumpEdgesReader dumper = new DumpEdgesReader(graph,outputFiles.get(0));
			dumper.execute();

			break;

		case "dump_edges_props":
			
			DumpEdgesWithPropsReader pdumper = new DumpEdgesWithPropsReader(graph, outputFiles.get(0));
			pdumper.execute();
			
			break;
			
		default:
			System.out.println("Scribe says: I do not understand " + queryType);
			break;
		}

		graph.shutdown();
	}

	protected CypherResultWriter decideCypherWriter(){
		CypherResultWriter writer = null;
		switch(outType.toLowerCase()){
		case "tab":
			writer = new CypherTabWriter();
			break;
			
		case "xgmml":
			writer = new CypherXGMMLWriter();
			break;

		default:
			System.out.println("Scribe says: I can not write " + queryType);
			break;
		}

		if(writer != null){
			writer.setOutputFile(outputFiles.get(0));
		}

		return writer;
	}
	
	protected GraphWriter decideGraphWriter(){
		GraphWriter writer = null;
		switch(outType.toLowerCase()){
		case "xgmml":
			writer = new DirectXGMMLWriter();
			writer.setWriter(outputFiles.get(0));
			writer.setGraphLabel(db);
			break;

		default:
			System.out.println("Scribe says: I can not write " + queryType);
			break;
		}

		return writer;
	}

}
