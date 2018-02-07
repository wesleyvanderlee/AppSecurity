package oracles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import com.bunq.learner.Query;
import com.bunq.learner.QueryCache;
import com.bunq.learner.SulAdapter;
import com.bunq.util.Property;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/*
 * Or not so random though?
 */
public class CustomRandomWalkEQOracle2
		implements EquivalenceOracle<MealyMachine<Integer, String, String, String>, String, String> {
	/**
	 * actual implementation
	 */
	public final RandomWalkEQOracle<String, String> test;
	Collection<? extends String> alphabet;

	ArrayList<DefaultQuery<String, String>> happyFlows;
	private Property properties;
	boolean first;
	// Indexes from the alphabet file to construct words from the symbols, repr
	// the happy flow
	private final int[][] happyFlowsIndexess = {};

	private final int[][] happyFlowsIndexes = { { 0,9,1,12,13,3 } };
	// private final int[][] happyFlowsIndexes = {};
	private int[] hfcount = { 0, 0, 0 };
	QueryCache cache;
	int count = 0;
	int counterexamplecount = 0;
	SulAdapter _sul;
	/*
	 * public void CustomRandomWalk(double restartProbability, long maxSteps,
	 * Random random, SulAdapter sul) {
	 * 
	 * this.test = new RandomWalkEQOracle<>(restartProbability, maxSteps, false,
	 * random, sul); first = true; happyFlows = new
	 * ArrayList<DefaultQuery<String, String>>(); this.properties =
	 * Property.getInstance(); }
	 */

	public CustomRandomWalkEQOracle2(double restartProbability, long maxSteps, Random random, SulAdapter sul,
			Collection<? extends String> _alphabet, QueryCache _cache) {
		this.cache = _cache;
		this.test = new RandomWalkEQOracle<>(restartProbability, maxSteps, false, random, sul);
		this._sul = sul;
		first = true;
		happyFlows = new ArrayList<DefaultQuery<String, String>>();
		alphabet = _alphabet;
		this.properties = Property.getInstance();
		this.initHappyFlows();
//		this.happyFlowToQueryCache();

	}

	public void initHappyFlows() {
		ArrayList<String> alphabetAsArray = new ArrayList<String>(this.alphabet);
		for (int[] flow : happyFlowsIndexes) {
			ArrayList<String> happySingleFlow = new ArrayList<String>();
			for (int ind : flow) {
				String symb = alphabetAsArray.get(ind);
				happySingleFlow.add(symb);
			}
			DefaultQuery<String, String> dq = new DefaultQuery<String, String>(Word.fromList(happySingleFlow));
			// dq.answer("0-OK"); // since it is a happy word
			// dq.answer(properties.get("failureMsg")); // since it is a happy
			// word
			happyFlows.add(dq);
//			System.out.println("Happy Flows!! " + happyFlows.toString());
		}

	}

	public void happyFlowToQueryCache() {
		ArrayList<String> alphabetAsArray = new ArrayList<String>(this.alphabet);
		for (int[] flow : happyFlowsIndexes) {
			Query forCacheQuery = new Query();
			for (int ind : flow) {
				String symb = alphabetAsArray.get(ind);
				forCacheQuery.appendWord(symb);
				forCacheQuery.addResult("0-OK");
			}
			this.cache.addQuery(forCacheQuery);
			// Partitions
			while (forCacheQuery.size() > 0) {
				forCacheQuery = forCacheQuery.removeLastItem();
				this.cache.addQuery(forCacheQuery);
			}
		}
	}

	public DefaultQuery<String, String> getHappyFlowWord(MealyMachine<Integer, String, String, String> hypothesis) {
		int i = 0;
		for (DefaultQuery<String, String> q : this.happyFlows) {

			hfcount[i]++;
			String answer = hypothesis.computeOutput(q.getInput()).lastSymbol();
			if (properties.get("failureMsg").equals(answer)) {
				// System.out.println("GETHAPPYFLOWWORD " + q + " -- " + answer
				// );
				// pose this query since happyflow is not accepted by the
				// hypothesis
				return q;
			}
			i++;
		}
		return null;
	}

	@Override
	public DefaultQuery<String, String> findCounterExample(MealyMachine<Integer, String, String, String> a,
			Collection<? extends String> clctn) {
		DefaultQuery<String, Word<String>> ce = this.test.findCounterExample(a, clctn);
		System.out.println("CustomoRandomWalk " + counterexamplecount++);
		if (ce != null) {
			System.out.println(ce.getInput().size());
//			System.out.println(ce);
			return new DefaultQuery<String, String>(ce.getInput(), ce.getOutput().lastSymbol());
		} else {
			return getHappyFlowWord(a);
		}
	}

	public DefaultQuery<String, String> findCounterExampleOld(MealyMachine<Integer, String, String, String> a,
			Collection<? extends String> clctn) {

		if (first) {

			first = false;
			//return toQuery(happyWord);
			ArrayList<String> happyWord = happyWord(clctn);

			DefaultQuery<String, String> dq = new DefaultQuery<String, String>(Word.fromList(happyWord));
	        WordBuilder<String> wbIn = new WordBuilder<>(Word.epsilon());
	        
			dq.answer(this._sul.step(wbIn.toString())); // since it is a happy word
			return dq;
		}
		DefaultQuery<String, Word<String>> ce = this.test.findCounterExample(a, clctn);
		System.out.println("AAAAAAAAA\n\n\n");
		System.out.println("CE " + ce.getOutput());
		if (ce == null) {
			ArrayList<String> happyWord = happyWord(clctn);
			System.out.println("HAPPYWORD: " + happyWord);
			String answer = a.computeOutput(happyWord).lastSymbol();
			System.out.println("HW ACCEPTED: " + answer);

			if (first) {

				first = false;
				//return toQuery(happyWord);
				DefaultQuery<String, String> dq = new DefaultQuery<String, String>(Word.fromList(happyWord));
		        WordBuilder<String> wbIn = new WordBuilder<>(Word.epsilon());
		        
				dq.answer(this._sul.step(wbIn.toString())); // since it is a happy word
				return dq;
			}
			/*
			 * if(answer.contains("NOTFOUND")){ System.out.println("\n\nHAPPL" +
			 * happyWord); System.out.println(a.computeOutput(happyWord) +
			 * " \n\n"); return toQuery(happyWord); }
			 */
			// this.
			// if(a.computeOutput(input))

			System.out.println("COUNTEREXAMPLE IS NULLLLLLLLL");
			System.out.println("HW: " + toQuery(happyWord));
			return null;
		}

		System.out.println();
		DefaultQuery<String, String> dq = new DefaultQuery<String, String>(ce.getInput(), ce.getOutput().lastSymbol());
		System.out.println(dq);
		return dq;
	}

	public DefaultQuery<String, String> toQuery(ArrayList<String> word) {
		DefaultQuery<String, String> dq = new DefaultQuery<String, String>(Word.fromList(word));
		dq.answer("0-OK"); // since it is a happy word
		return dq;
	}

	public ArrayList<String> happyWord(Collection<? extends String> clctn) {
		ArrayList<String> happywordd = new ArrayList<String>();

		int i = 0;
		for (String s : clctn) {
			if (i > 0 && i < 6)
				happywordd.add(s);
			i++;
		}
		return happywordd;

	}

}