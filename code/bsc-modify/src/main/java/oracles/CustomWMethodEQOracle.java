package oracles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.bunq.util.Property;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class CustomWMethodEQOracle<A extends UniversalDeterministicAutomaton<Integer, I, String, String, String> & Output<I, D>, I, D>
		implements EquivalenceOracle<A, I, D> {

	private int maxDepth;
	private final MembershipOracle<I, D> sulOracle;
	private String stamp;
	private Property properties;
	private int hypCount;
	private final String hyploc = "hyp/";

	/**
	 * Constructor.
	 *
	 * @param maxDepth
	 *            the maximum length of the "middle" part of the test cases
	 * @param sulOracle
	 *            interface to the system under learning
	 */
	public CustomWMethodEQOracle(int maxDepth, MembershipOracle<I, D> sulOracle) {
		this.maxDepth = maxDepth;
		this.sulOracle = sulOracle;
		this.properties = Property.getInstance();
		this.stamp = properties.get("alphabetFile") + "-" + numberOfFilesInLoc(hyploc) + "-";
		this.hypCount = 0;
	}

	public int numberOfFilesInLoc(String loc) {
		File f = new File(loc);
		if (!f.isDirectory()) {
			System.out.println(hyploc + " is not a valid directory ");
			return 0;
		}
		File[] files = f.listFiles();
		return files.length;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public String write(A hypothesis, Collection<? extends I> inputs) {
		try {
			String filename = hyploc + stamp + hypCount;
			CustomGraphDOT.write(hypothesis, inputs, new PrintStream(new FileOutputStream(hyploc + stamp + hypCount)));
//			hypCount++;
			return filename;

		} catch (IOException e) {
			System.out.println("Error writing hypothesis to go-format");
			e.printStackTrace();
		}
		return null;
	}

	private List<Word<I>> getMinimalSeparatingSequences(A hypothesis, Collection<? extends I> inputs) {
		ArrayList<String> alphabetAsArray = new ArrayList(inputs);

		// 1. Write hypothesis to custom .dot format
		String loc = write(hypothesis, inputs);
//		loc = "hyp/hyp.dot";
		// 2. Run go command
		System.out.println("[i]\t\tGo script running to determine characterizing set");
		String goCommand = String.format("scripts/runminsepseqs %s", loc);
		String res = executeCommand(goCommand);
		// 3. res contains combinations of indexes in the following form:
		/*
		 * [0];[0];[0];[0];[0];[0];[0];[2];[3];[1];[4];[2];[0];[2];[2];[1];[2];[
		 * 5];[0];[3];[2];[1];[3];[2]; or 
		 * 
		 * [2 2 1];[2 1];[1];[0];[2 2 1];[2 1];[1];[0];[2 2 1];[2 2 1];[2 1];[1];[0];[2 1];[2 1];[2 1];[1];
		 */

		// Generate a list of separating sequences from the above
		return getSepSeqFromGoOutput(res, alphabetAsArray);
	}

	private ArrayList<Word<I>> getSepSeqFromGoOutput(String out, ArrayList<String> alphabetLookup) {
		ArrayList<Word<I>> minSepSeq = new ArrayList<Word<I>>();

		String[] parts = out.split(";");
		String seen = "";
		for (String part : parts) {
			if(seen.contains(part)){
				continue;
			}else{
				// It will be seen in this iteration
				seen += part;
			}
			// remove brackets
			part = part.replace("[", "").replace("]", "");
			// Multiple elements
			Word w = null;
			if (part.contains(" ")) {
				ArrayList<String> sequence = new ArrayList<String>();
				String[] els = part.split(" ");
				for (String el : els) {
					String symb = alphabetLookup.get(Integer.parseInt(el));
					sequence.add(symb);
				}
				w = Word.fromList(sequence);
				minSepSeq.add(w);
			} else {
				try {
					w = Word.fromLetter(alphabetLookup.get(Integer.parseInt(part)));
				} catch (NumberFormatException e) {
					//Might be empty sets (i.e. for same states) from go script
					continue;
				}
			}
			if (!minSepSeq.contains(w)) {
				minSepSeq.add(w);
			}
		}
		return minSepSeq;
	}

	/*
	 * a between 0 and 100
	 */
	public List<Word<I>> shrink(List<Word<I>> l, int a){
        Random randomGenerator = new Random();
        List<Word<I>> res = new ArrayList<Word<I>>();
		for(Word<I> w : l){
			if(randomGenerator.nextInt(100) < a){
				res.add(w);
			}
		}
		return res;
	}
	
	
	@Override
	public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
		System.out.println("In findCounterExample of CustomWMethod");
		
		System.out.println("Hypothesis");
		try {
			GraphDOT.write(hypothesis, inputs, System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//////////
		List<Word<I>> transCover = Automata.transitionCover(hypothesis, inputs);
		List<Word<I>> middleTuples =
				Lists.newArrayList(Iterables.transform(CollectionsUtil.allTuples(inputs, 0, 4), Word::fromList));
        List<Word<I>> characterizingSet = getMinimalSeparatingSequences(hypothesis, inputs);
		List<Word<I>> charSuffixesOld = Automata.characterizingSet(hypothesis, inputs);
		characterizingSet = charSuffixesOld;

    	System.out.println("Minsep:");
		System.out.println("Total words: "+ characterizingSet.size());
		System.out.println("Total els: " + totalWords(characterizingSet));
		
		System.out.println("OLD:");
		System.out.println("Total words: "+ charSuffixesOld.size());
		System.out.println("Total els: " + totalWords(charSuffixesOld));

        
        
        // Special case: List of characterizing suffixes may be empty,
        // but in this case we still need to test!
        if (characterizingSet.isEmpty()) {
            characterizingSet = Collections.singletonList(Word.epsilon());
        }

        
        System.out.println("Transcover: " + transCover.size());
        System.out.println("middleTuples: " + middleTuples.size());
        System.out.println("characterizingSet: " + characterizingSet.size());
        
//        middleTuples = Collections.singletonList(Word.epsilon());
        
        System.out.println("MT.size1 " + middleTuples.size());
        middleTuples = shrink(middleTuples,5);
        System.out.println("MT.size2 " + middleTuples.size());
        ArrayList<Word<I>> allcombos = new ArrayList<Word<I>>();
        Random randomGenerator = new Random();
        for(Word<I> one : transCover){
        	for(Word<I> two : middleTuples){
        		for(Word<I> three : characterizingSet){
       			if(randomGenerator.nextInt(100) < 5) // Reduce test space, play with this for a while, else equivalence testing takes too long
        				allcombos.add(Word.fromWords(one, two, three));
        			
        		}
        	}
        }
        /*
        System.out.println("Created combos of size: " + allcombos.size());
        System.out.println("transcover size : " + transCover.size());
        System.out.println("middleTuples size : " + Lists.newArrayList(middleTuples).size());
        System.out.println("characterizingSet size : " + characterizingSet.size());
*/
        
        
        
        for(Word<I> possibleCounterExample : allcombos){
//        	System.out.println("El: " + hypCount + " size: " + possibleCounterExample.length());
        	DefaultQuery<I, D> query = new DefaultQuery<>(possibleCounterExample);
			D hypOutput = hypothesis.computeOutput(possibleCounterExample);
			sulOracle.processQueries(Collections.singleton(query));
			if (!Objects.equals(hypOutput, query.getOutput())) {
				hypCount++;
				// This is a valid counterexample
				return query;
			}
        }
        
        
//        while(wMethodIter.iterator().hasNext()){
//        	DefaultQuery<I, D> query = new DefaultQuery<>((Word<I>) wMethodIter.iterator().next());
//			D hypOutput = hypothesis.computeOutput((Iterable<? extends I>) query);
//			sulOracle.processQueries(Collections.singleton(query));
//			if (!Objects.equals(hypOutput, query.getOutput())) {
//				hypCount++;
//				return query;
//			}
//        }
		// No hypcount increasing, as at this point the hypothesis is equivalent
		// to the sut
		return null;
	}
	
	public int totalWords(List<Word<I>> w){
		int i = 0;
		for(Word<I> wi : w){
			i += wi.length();
		}
		return i;
	}
	
//	@Override
	public DefaultQuery<I, D> findCounterExampleOld(A hypothesis, Collection<? extends I> inputs) {
		System.out.println("In findCounterExample of CustomWMethod");

		List<Word<I>> transCover = Automata.transitionCover(hypothesis, inputs);
		List<Word<I>> charSuffixesOld = Automata.characterizingSet(hypothesis, inputs);
		List<Word<I>> charSuffixes = getMinimalSeparatingSequences(hypothesis, inputs);
		
//		System.out.println("[i]\t\tGo script found " +charSuffixes.size() + " elements. (Original: " + charSuffixesOld.size()+ ").") ;
		System.out.println("Minsep:");
		System.out.println("Total words: "+ charSuffixes.size());
		System.out.println("Total els: " + totalWords(charSuffixes));
		
		System.out.println("OLD:");
		System.out.println("Total words: "+ charSuffixesOld.size());
		System.out.println("Total els: " + totalWords(charSuffixesOld));
		
		// Special case: List of characterizing suffixes may be empty,
		// but in this case we still need to test the transcover!
		if (charSuffixes.isEmpty()) {
			charSuffixes = Collections.singletonList(Word.<I>epsilon());
		}

		WordBuilder<I> wb = new WordBuilder<>();

		DefaultQuery<I, D> query;
		D hypOutput;
		String output;
		Word<I> queryWord;

		for (Word<I> trans : transCover) {
			query = new DefaultQuery<>(trans);
			sulOracle.processQueries(Collections.singleton(query));

			hypOutput = hypothesis.computeOutput(trans);
			if (!Objects.equals(hypOutput, query.getOutput()))
				return query;

			for (Word<I> suffix : charSuffixes) {
				wb.append(trans).append(suffix);
				queryWord = wb.toWord();
				wb.clear();
				query = new DefaultQuery<>(queryWord);
				hypOutput = hypothesis.computeOutput(queryWord);
				sulOracle.processQueries(Collections.singleton(query));
				if (!Objects.equals(hypOutput, query.getOutput())) {
					hypCount++;
					return query;
				}
			}
		}
		// No hypcount increasing, as at this point the hypothesis is equivalent
		// to the sut
		return null;
	}

	private String executeCommand(String command) {
		String info = "";
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(command);
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";

			while ((line = b.readLine()) != null) {
				info += line + "\n";
			}
			b.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

}