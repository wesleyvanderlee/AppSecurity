package com.bunq.learner;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;

/**
 * Class for a query (list of words) that can be asked to the teacher.
 * 
 * @author tom
 *
 */
public class Query {
	private ArrayList<String> words;
	private ArrayList<String> results;

	public String toString() {
		String res = "";
		for (String w : words) {
			res += w + ";";
		}
		res += ";"; // two times ;; to seperate parts!
		for (String w : results) {
			res += w + ";";
		}
		return res;
	}

	public Query() {
		words = new ArrayList<String>();
		results = new ArrayList<String>();
	}

	public Query(ArrayList<String> wordsLst, ArrayList<String> resultsLst) {
		words = wordsLst;
		results = resultsLst;
	}

	public int size() {
		return this.words.size();
	}

	/**
	 * Returns a new query object with the words of this query, including the
	 * word given here. The original Query will not be edited.
	 * 
	 * @param word
	 *            the word that should be appended to the query.
	 * @return a new Query including the given word
	 */
	public Query addWord(String word) {
		ArrayList<String> newWords = (ArrayList<String>) words.clone();
		newWords.add(word);
		return new Query(newWords, results);
	}

	public void appendWord(String word) {
		this.words.add(word);
	}

	/**
	 * Adds a result to the resultList of this query.
	 * 
	 * @param result
	 *            the result you want to add.
	 */
	public void addResult(String result) {
		results.add(result);
	}

	public ArrayList<String> getWords() {
		return words;
	}

	public String getResult(int index) {
		return results.get(index);
	}

	public String getLastResult() {
		return getResult(results.size() - 1);
	}

	/**
	 * Removes the last combination of word and answer.
	 **/
	public Query removeLastItem() {
		ArrayList<String> newWords = (ArrayList<String>) this.words.clone();
		ArrayList<String> newResults = (ArrayList<String>) this.results.clone();

		newWords.remove(words.size() - 1);
		newResults.remove(results.size() - 1);

		return new Query(newWords, newResults);
	}

	/**
	 * Generates a hashcode using Apache's HashCodeBuilder.
	 */
	public int hashCode() {
		// do not use results here: in QueryCache, we need to compare
		// on the basis of the words, not the results
		return new HashCodeBuilder(23, 83).append(words).toHashCode();
	}

	/**
	 * Compares the hashcodes of the two objects.
	 */
	public boolean equals(Object other) {
		try {
			Query otherQ = (Query) other;
			return otherQ.hashCode() == this.hashCode();
		} catch (ClassCastException exc) {
			return false;
		}
	}
}
