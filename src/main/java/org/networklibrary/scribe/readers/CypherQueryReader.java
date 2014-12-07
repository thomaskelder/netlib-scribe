package org.networklibrary.scribe.readers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.networklibrary.scribe.writers.CypherResultWriter;

public class CypherQueryReader {

	private GraphDatabaseService graph;
	private CypherResultWriter cypherWriter;
	private ExecutionEngine engine;

	public CypherQueryReader( GraphDatabaseService graph, CypherResultWriter cypherWriter){
		this.graph = graph;
		this.cypherWriter = cypherWriter;
		
		engine = new ExecutionEngine( graph );
	}

	public void executeCypher(String query){

		try (Transaction tx = graph.beginTx()){

			ExecutionResult res = engine.execute(query);

			Map<String, List<Object>> queryRes = new HashMap<String,List<Object>>();

			cypherWriter.setColumns(res.columns());

			for(Map<String,Object> row : res){
				cypherWriter.addRow(row);
			}
		}
	}
}
