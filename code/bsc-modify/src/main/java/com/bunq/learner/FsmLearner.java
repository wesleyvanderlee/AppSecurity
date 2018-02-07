package com.bunq.learner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Random;

import com.bunq.util.Property;

import de.learnlib.algorithms.dhc.mealy.MealyDHCBuilder;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.algorithms.malerpnueli.MalerPnueliMealyBuilder;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.SUL;
import de.learnlib.cache.sul.SULCaches;
import de.learnlib.eqtests.basic.WMethodEQOracle;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.experiments.Experiment.MealyExperiment;
import de.learnlib.oracles.ResetCounterSUL;
import de.learnlib.oracles.SULOracle;
import de.learnlib.statistics.SimpleProfiler;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SimpleAlphabet;
import oracles.CustomRandomWalkEQOracle;
import oracles.CustomWMethodEQOracle;
import oracles.RandomWalkWalk;
import oracles.WMethodEQOracle1;

/**
 * This class 'runs' the entiring learning process. It has the most bindings
 * with LearnLib of all classes.
 * 
 * @author tom
 *
 */
public class FsmLearner {
	public Alphabet<String> alphabet;
	private ResetCounterSUL<String, String> statisticSul;
	public SulAdapter sul;
	private SUL<String, String> effectiveSul;
	private SULOracle<String, String> oracle;
	private MealyExperiment experiment;
	public MealyMachine<Integer, String, String, String> result;
	private String stamp;
	// private MealyExperiment<String, String> experiment;

	private static Property properties = Property.getInstance();

	MealyLearner<String, String> learnAlg;
	
	public MealyLearner getlearner(){
		return this.learnAlg;
	}

	/**
	 * Loads alphabet for the specified OS
	 * 
	 * @param forOS
	 *            can be ios or android.
	 * @throws FileNotFoundException
	 *             when no alphabet in the format alphabet_forOs.txt can be
	 *             found in the alphabet folder in the root folder of this
	 *             project.
	 */
	private void loadAlphabet() throws FileNotFoundException {
		File alphabetFile = new File("alphabet/" + properties.get("alphabetFile") + ".txt");
		if (!alphabetFile.exists()) {
			throw new FileNotFoundException("Please create an alphabet first");
		}

		alphabet = new SimpleAlphabet<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(alphabetFile));
			String line;
			while ((line = reader.readLine()) != null) {
				alphabet.add(line);
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Error while reading alphabet! " + e.getMessage());
		}
	}

	/**
	 * Creates a mealyLearner of with the L* learning algorithm.
	 * 
	 * @return a MealyLearner instance
	 */
	private MealyLearner<String, String> getLStarLearner() {
		return new ExtensibleLStarMealyBuilder<String, String>().withAlphabet(alphabet) // input
																						// alphabet
				.withOracle(oracle) // membership oracle
				.create();
	}

	/**
	 * Creates a mealyLearner of with the Maler/Pnueli learning algorithm.
	 * 
	 * @return a MealyLearner instance
	 */
	private MealyLearner<String, String> getMalerPnueliLearner() {
		return new MalerPnueliMealyBuilder<String, String>().withAlphabet(alphabet) // input
																					// alphabet
				.withOracle(oracle) // membership oracle
				.create();
	}

	/**
	 * Creates a mealyLearner of with the Kearns/Varizani learning algorithm.
	 * 
	 * @return a MealyLearner instance
	 */
	private MealyLearner<String, String> getKearnsVarizaniLearner() {
		return new KearnsVaziraniMealyBuilder<String, String>().withAlphabet(alphabet) // input
																						// alphabet
				.withOracle(oracle) // membership oracle
				.create();
	}

	/**
	 * Creates a mealyLearner of with the Direct Hypothesis Construction
	 * learning algorithm.
	 * 
	 * @return a MealyLearner instance
	 */
	private MealyLearner<String, String> getDHCLearner() {
		return new MealyDHCBuilder<String, String>().withAlphabet(alphabet) // input
																			// alphabet
				.withOracle(oracle) // membership oracle
				.create();
	}

	/**
	 * Creates a mealyLearner of with the TTT learning algorithm.
	 * 
	 * @return a MealyLearner instance
	 */
	private MealyLearner<String, String> getTTTLearner() {
		return new TTTLearnerMealyBuilder<String, String>().withAlphabet(alphabet) // input
				// alphabet
				.withOracle(oracle) // membership oracle
				.create();
	}

	/**
	 * Read the algorithm to use from the config file.
	 * 
	 * @return a MealyLearner instance
	 */
	private MealyLearner<String, String> getLearner() {
		switch (properties.get("learningAlgorithm")) {
		case "lstar":
			return getLStarLearner();
		case "TTT":
			return getTTTLearner();
		case "DHC":
			return getDHCLearner();
		case "Maler/Pnueli":
			return getMalerPnueliLearner();
		case "Kearns/Varizani":
			return getKearnsVarizaniLearner();
		default:
			return getLStarLearner();
		}
	}

	/**
	 * Instantiates the adapters, suls and oracles. Uses the alphabet field to
	 * instantiate the sulcache
	 */
	private void instantiateSuls() {
		sul = new SulAdapter();
		// oracle for counting queries wraps sul
		statisticSul = new ResetCounterSUL<>("membership queries", sul);

		effectiveSul = statisticSul;
		// use caching in order to avoid duplicate queries
		//effectiveSul = SULCaches.createCache(alphabet, effectiveSul);
		oracle = new SULOracle<>(effectiveSul);
	}

	public void instantiateSulsReset() {
		sul.conflictReset();
		// oracle for counting queries wraps sul
		statisticSul = new ResetCounterSUL<>("membership queries", sul);

		effectiveSul = statisticSul;
		// use caching in order to avoid duplicate queries
		effectiveSul = SULCaches.createCache(alphabet, effectiveSul);
		oracle = new SULOracle<>(effectiveSul);
	}

	private EquivalenceOracle<MealyMachine<Integer, String, String, String>, String, String> getEquivalanceOracle() {
		
		
		switch (properties.get("EquivMethod")) {
		case "RandomWalk":
			return new RandomWalkEQOracle(0.05, // reset SUL w/ this probability
					// before a step
					40, // max steps (overall)
					false, // reset step count after counterexample
					new Random(46346293), // make results reproducible
					sul // system under learning
			);
		case "RandomWalkWalk":
			return new RandomWalkWalk(0.05, // reset SUL w/ this probability
					// before a step
					40, // max steps (overall)
					false, // reset step count after counterexample
					new Random(46346293), // make results reproducible
					sul // system under learning
			);
		case "CustomRandomWalk":
			return new CustomRandomWalkEQOracle(0.05, // reset SUL w/ this probability
					// before a step
					40, // max steps (overall)
					new Random(46346293), // make results reproducible
					sul, // system under learning
					alphabet, this.sul.getQueryCache());
		case "WMethod":
			return new WMethodEQOracle(4, this.oracle);
		case "WMethodONE":
			return new WMethodEQOracle1(4, this.oracle);
		case "CustomWMethod":
			return new CustomWMethodEQOracle(4, this.oracle);
		default:
			System.out.println("[i]\tUnmatched Equivalence method: " + properties.get("EquivMethod"));
			System.out.println("[i]\tUsing Equivalence method: CustomRandomWalk");
			return new CustomRandomWalkEQOracle(0.05, // reset SUL w/ this probability
					// before a step
					40, // max steps (overall)
					new Random(46346293), // make results reproducible
					sul, // system under learning
					alphabet, this.sul.getQueryCache());
		}
	}

	private EquivalenceOracle<MealyMachine<Integer, String, String, String>, String, String> getEquivalanceOracle2() {
		return new WMethodEQOracle(10, this.oracle);
	}

	private EquivalenceOracle<MealyMachine<Integer, String, String, String>, String, String> getEquivalanceOracleold() {

		return new RandomWalkEQOracle(0.05, // reset SUL w/ this probability
											// before a step
				40, // max steps (overall)
				false, // reset step count after counterexample
				new Random(46346293), // make results reproducible
				sul // system under learning
		);

		/*
		 * return new RandomWalkEQOracle(0.05, // reset SUL w/ this probability
		 * before a step 40, // max steps (overall) false, // reset step count
		 * after counterexample new Random(46346293), // make results
		 * reproducible sul // system under learning );
		 */
	}

	/**
	 * Sets the experiment field. Should be called when the alphabet has been
	 * loaded already.
	 * 
	 * @param learningAlg
	 *            the learning algorithm for the experiment (like extensible l*)
	 * @param oracle
	 *            the equivalence oracle that will be used for finding
	 *            counterexamples.
	 */
	private void instantiateExperiment(MealyLearner learningAlg,
			EquivalenceOracle<MealyMachine<Integer, String, String, String>, String, String> eqOracle) {
		// private void instantiateExperiment(MealyLearner<String, String>
		// learningAlg,

		// EquivalenceOracle<? super MealyMachine<?, String, ?, String>, String,

		experiment = new MealyExperiment(learningAlg, eqOracle, alphabet);
	}

	/**
	 * Prepares everything. After this method, the runExperiment method can be
	 * invoked.
	 */
	public void setUpLearner() throws FileNotFoundException {
		loadAlphabet();
		instantiateSuls();

		this.learnAlg = getLearner();
		EquivalenceOracle<MealyMachine<Integer, String, String, String>, String, String> eqOracle = getEquivalanceOracle();

		instantiateExperiment(learnAlg, eqOracle);
	}

	/**
	 * Should only be invoked after setUpLearner(). This function will be active
	 * the most of the time, during the execution of the experiment. It outputs
	 * it findings in the terminal.
	 * 
	 */
	public void runExperiment() throws Exception {
		// turn off time profiling
		experiment.setProfile(true);
		// enable logging of models
//		experiment.setLogModels(true);

		experiment.setLogModels(false);

		// run experiment
		// MealyMachine<?, String, ?, String> finalHyp = experiment.run();
		MealyMachine<Integer, String, String, String> finalHyp = (MealyMachine<Integer, String, String, String>) experiment
				.run();

		// Visualization.visualizeAutomaton(finalHyp, alphabet, true);

	}

	/*
	 * SAME as setUpLearner but no additional appium connection should be made
	 */
	public void handleConflictException() {
		// QueryCache qc = this.sul.getQueryCache();
		System.out.println("In conflict exception handling mode");
		try {
			loadAlphabet();
		} catch (Exception e) {
			//Not even necessary
		}
		instantiateSuls();

		this.learnAlg = getLearner();
		EquivalenceOracle<MealyMachine<Integer, String, String, String>, String, String> eqOracle = getEquivalanceOracle();

		instantiateExperiment(learnAlg, eqOracle);

	}
	


	/**
	 * Stops the experiment by closing the sulAdapter and implicitly stopping
	 * the teacher.
	 */
	public void stopExperiment() {
		sul.stop();
	}

	/**
	 * Print the results of the learning process. Should be invoked after the
	 * runExperiment function.
	 * 
	 * @throws IOException
	 *             when the printing of the diagram goes wrong
	 */
	public void printResults() throws IOException {

		try {
			// new
			// ObservationTableASCIIWriter<MealyMachine<>().write(((AbstractLStar<MealyMachine<?,
			// String, ?, String>, String, Word<String>>)
			// this.learnAlg).getObservationTable(), System.out);
			// System.out.println("^^ Final observation table ^^");
		} catch (Exception e) {
			System.out.println("***** UNABLE TO PRINT OBSERVATION TABLE");
		}

		// ObservationTable<String,Word<String>> ot =
		// ((AbstractLStar<MealyMachine<?, String, ?, String>, String,
		// Word<String>>) this.learnAlg).get.getObservationTable();
		// MealyMachine<?, String, ?, String> result =
		// experiment.getFinalHypothesis();
		this.result = (MealyMachine<Integer, String, String, String>) experiment.getFinalHypothesis();

		// report results
		System.out.println("-------------------------------------------------------");

		System.out.println("Counts " + sul.getCounts());
		System.out.println("actionLengthStatistics " + sul.getActionLengthStatistics());
		
		// profiling
		try {
			System.out.println(SimpleProfiler.getResults());

		} catch (Exception e) {
			System.out.println("[-]\tUnable to print profiles");
		}

		// learning statistics
		try {
			System.out.println(experiment.getRounds().getSummary());
			System.out.println(statisticSul.getStatisticalData().getSummary());
		} catch (Exception e) {
			System.out.println("[-]\tUnable to print learning satistics");
		}

		// model statistics
		try {
			System.out.println("States: " + result.size());
			System.out.println("Sigma: " + alphabet.size());
		} catch (Exception e) {
			System.out.println("[-]\tUnable to print model statistics");
		}

		stamp = "graphs/graph_" + (new Date()).getTime();

		System.out.println();
		System.out.println("Model: ");
		try {

			// GraphDOT.write(result, alphabet, System.out);
			GraphDOT.write(result, alphabet, new PrintStream(new FileOutputStream(stamp)));
			Runtime.getRuntime().exec("python shortlabels.py " + stamp);
			System.out.println("[i]\tRead output from " + stamp + "_shortened");

		} catch (Exception e) {
			System.out.println("[-]\tUnable to print graph");
			e.printStackTrace();
		}
	}


	public String getStamp() {
		return this.stamp;
	}

}
