package org.networklibrary.scribe.writers.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.networklibrary.scribe.writers.CypherResultWriter;

public class CypherXGMMLWriter implements CypherResultWriter {

	protected static final Logger log = Logger.getLogger(CypherXGMMLWriter.class.getName());

	protected List<String> cols;
	protected Map<String,ResType> colType = new HashMap<String,ResType>();

	DirectXGMMLWriter xgmmlWriter = null;

	public CypherXGMMLWriter(){
		xgmmlWriter = new DirectXGMMLWriter();
		xgmmlWriter.setGraphLabel("cypher-query");
	}

	@Override
	public void setColumns(List<String> columns) {
		System.out.println(columns);
		cols = columns;
		for(String col : cols){
			colType.put(col,ResType.Unknown);
		}
	}

	@Override
	public void addRow(Map<String, Object> row) {
		for(Entry<String,Object> rowItem : row.entrySet()){
			Object item = rowItem.getValue();
			String col = rowItem.getKey();

			ResType type = duckTypeObject(item, col);

			switch(type){
			case Node:
				xgmmlWriter.addNode((Node)item);
				break;

			case Edge:
				xgmmlWriter.addEdge((Relationship)item);
				break;

			default:
				break;

			}
		}
	}

	@Override
	public void setOutputFile(String outputFile) {
		xgmmlWriter.setWriter(outputFile);
	}

	@Override
	public void finishUp() {
		xgmmlWriter.finishUp();
	}
	
	protected ResType duckTypeObject(Object obj,String column){

		ResType result = colType.get(column);

		if(result == ResType.Unknown){
			if(isNodeType(obj)){
				result = ResType.Node;
			} else if(isEdgeType(obj)){
				result = ResType.Edge;
			} else { // this could / should be extended
				result = ResType.Ignore;
			}
			colType.put(column, result);
		}

		return result;
	}

	protected boolean isNodeType(Object obj){
		try{

			return obj instanceof Node;

		} catch(ClassCastException e){
			return false;
		}
	}

	protected boolean isEdgeType(Object obj){
		try{

			return obj instanceof Relationship;

		} catch(ClassCastException e){
			return false;
		}
	}

	protected enum ResType {
		Node,
		Edge,
		Ignore,
		Unknown
	}

}
