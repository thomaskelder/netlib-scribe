package org.networklibrary.scribe.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataFromFiles {

	/**
	 * Reads and trims each line from a file into a list.
	 * @param f input file
	 * @return trimmed lines
	 * @throws IOException 
	 */
	static public List<String> readFromFile(File f) throws IOException {
		List<String> ids = null;

		BufferedReader r = new BufferedReader(new FileReader(f));
		ids = new ArrayList<String>();

		while(r.ready()){
			String line = r.readLine();
			ids.add(line.trim());
		}
		r.close();

		return ids;
	}

}
