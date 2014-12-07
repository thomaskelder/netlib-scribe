package org.networklibrary.scribe.writers.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.networklibrary.scribe.writers.CypherResultWriter;

public class CypherTabWriter implements CypherResultWriter {

	private BufferedWriter writer;
	private List<String> columns;

	@Override
	public void setColumns(List<String> columns) {
		this.columns = columns;

		writeTo(columns.get(0));
		for(int i = 1; i < columns.size(); ++i){
			writeTo("\t" + columns.get(i));
		}
		writeTo("\n");
		flushAll();
	}

	@Override
	public void addRow(Map<String, Object> row) {
		writeTo(row.get(columns.get(0)).toString());
		for(int i = 1; i < columns.size(); ++i){
			writeTo("\t" + row.get(columns.get(i)).toString());
		}
		writeTo("\n");
		flushAll();
	}

	@Override
	public void setOutputFile(String outputFile) {
		try{

			writer = new BufferedWriter(new FileWriter(outputFile));

		}catch(IOException e){
			System.out.println("Failed to open files: " + e.getMessage());
			writer = null;
		}
	}

	protected void writeTo(String text){
		try{
			if(writer != null)
				writer.write(text);

		} catch(IOException e){
			System.out.println("Failure at writing: " + e.getMessage());
		}
	}

	protected void flushAll(){
		try{
			if(writer != null)
				writer.flush();

		} catch(IOException e){
			System.out.println("Failed to flush: " + e.getMessage());
		}
	}

	public void finishUp(){
		try{
			if(writer != null)
				writer.close();

		} catch(IOException e){
			System.out.println("Failed to close: " + e.getMessage());
		}
	}

}
