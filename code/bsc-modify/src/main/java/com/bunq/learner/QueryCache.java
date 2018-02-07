package com.bunq.learner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This class contains cached queries. Because the instrumentation process of
 * mobile applications is really slow, we can speed up the process by saving
 * results and using those results for queries that have 'a dead-end'.
 * 
 * @author tom
 *
 */
public class QueryCache {

	/*
	 * If filename_IN == filename_OUT: the cache will be read and added with new
	 * queries.
	 */
	String filename_IN = "QueryCache";
	String filename_OUT = "QueryCache";
	boolean HARDQUERYCACHEREAD = false;
	boolean HARDQUERYCACHEWRITE = false;

	private HashMap<Integer, Query> queryMap;
	BufferedWriter bw = null;
	FileWriter fw = null;
	BufferedReader br = null;
	FileReader fr = null;

	public QueryCache() {
		queryMap = new HashMap<>();

		System.out.println("Read from cache: " + HARDQUERYCACHEREAD + " Write to cache: " + HARDQUERYCACHEWRITE);

		if (HARDQUERYCACHEWRITE) {
			setupWriter();
		}
		if (HARDQUERYCACHEREAD) {
			setupReader();
			read();
		}
	}

	public void read() {
		try {

			fr = new FileReader(filename_IN);
			br = new BufferedReader(fr);

			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename_IN));

			while ((sCurrentLine = br.readLine()) != null) {
				try {
					String[] parts = sCurrentLine.split(";;");
					ArrayList<String> wordsLst = new ArrayList<String>();
					ArrayList<String> resultsLst = new ArrayList<String>();
					for (String word : parts[0].split(";")) {
						wordsLst.add(word.replace(";", ""));
					}
					for (String result : parts[1].split(";")) {
						resultsLst.add(result.replace(";", ""));
					}
					this.addQueryFromCache(new Query(wordsLst, resultsLst));
				} catch (Exception e) {
//					System.out.println("[i]\tSkipping malformed QueryCacheLine: " + sCurrentLine);
				}
			}

		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Successfully read cache from file with size: " + this.queryMap.keySet().size());
	}

	public void setupWriter() {
		File file = new File(filename_OUT);
		Scanner readerr = null;
		try {
			// if file doesnt exists, then create it
			readerr = new Scanner(System.in); // Reading from System.in
			// System.out.println("Delete old cachefile " + filename_OUT + "?
			// (yes/misc)");
			String del = "no";
			if (readerr != null) {
				// System.out.println(readerr.toString());
				// del = readerr.next();
			}
			boolean delete = "yes".equals(del);
			if (delete && file.exists()) {
				System.out.println(" Trying to delete");
				file.delete();
			}
			file.createNewFile();
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (readerr != null)
				readerr.close();
		}
	}

	public void setupReader() {
		try {
			this.fr = new FileReader(filename_IN);
			this.br = new BufferedReader(fr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a query to the hashmap.
	 * 
	 * @param query
	 *            the query you want to add
	 */
	public void addQuery(Query query) {
		if (!queryMap.containsKey(query.hashCode())) {
			queryMap.put(query.hashCode(), query);
			if (HARDQUERYCACHEWRITE) {
				writeQueryToCache(query);
			}
		}
	}

	/**
	 * Add a query to the hashmap.
	 * 
	 * @param query
	 *            the query you want to add
	 */
	public void addQueryFromCache(Query query) {
		if (!queryMap.containsKey(query.hashCode())) {
			queryMap.put(query.hashCode(), query);
		}
	}

	public void writeQueryToCache(Query query) {
		try {
			bw.write(query + "\n");
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the hashmap contains this query.
	 * 
	 * @param query
	 *            the query we want to check
	 * @return true if the query is in the hashmap, false otherwise
	 */
	public boolean hasQuery(Query query) {
		return queryMap.containsKey(query.hashCode());
	}

	/**
	 * Gets the result for the given query.
	 * 
	 * @param query
	 *            the query of which we want to retrieve the result
	 * @return the result of the given query
	 * @throws QueryCacheMissException
	 *             when the query is not present in the cache
	 */
	public String getResultForQuery(Query query) throws QueryCacheMissException {
		if (hasQuery(query)) {
			return queryMap.get(query.hashCode()).getLastResult();
		}
		throw new QueryCacheMissException("Entry not found");
	}

	public void print() {
		System.out.println("[Cache:  ");
		for (Integer i : queryMap.keySet()) {
			String key = i.toString();
			String value = queryMap.get(i).toString();
			System.out.println(key + ", " + value);
		}
		System.out.println(" ]");
	}
}
