package org.networklibrary.scribe.writers;

import java.util.List;
import java.util.Map;

public interface CypherResultWriter {

	
	public void setColumns(List<String> columns);
	public void addRow(Map<String, Object> row);
	public void setOutputFile(String outputFile);
	public void finishUp();
}
