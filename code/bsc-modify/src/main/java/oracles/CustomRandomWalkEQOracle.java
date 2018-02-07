//https://github.com/LearnLib/learnlib/blob/develop/oracles/equivalence-oracles/src/main/java/de/learnlib/oracle/equivalence/mealy/RandomWalkEQOracle.java
package oracles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.bunq.learner.QueryCache;
import com.bunq.learner.SulAdapter;
import com.bunq.util.Property;

import de.learnlib.api.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.SUL;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Performs a random walk over the hypothesis. A random walk restarts with a fixed probability after every step and
 * terminates after a fixed number of steps or with a counterexample. The number of steps to termination may be reset
 * for every new search.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author falkhowar
 */
public class CustomRandomWalkEQOracle<I, O> implements MealyEquivalenceOracle<I, O> {

    /**
     * probability to restart before step.
     */
    private final double restartProbability;

    /**
     * maximum number of steps.
     */
    private final long maxSteps;
    /**
     * RNG.
     */
    private final Random random;
    /**
     * System under learning.
     */
    private final SUL<I, O> sul;
    /**
     * step counter.
     */
    private long steps;
    /**
     * flag for reseting step count after every search.
     */
    private boolean resetStepCount;
    
    int hc;
    
	Collection<? extends String> alphabet;

	ArrayList<DefaultQuery<I, Word<O>>> happyFlows;
	private Property properties;
	boolean first;
	// Indexes from the alphabet file to construct words from the symbols, repr
	// the happy flow

	private final int[][] happyFlowsIndexes = { { 0,9,1,12,13,3 } };
	// private final int[][] happyFlowsIndexes = {};
	private int[] hfcount = { 0, 0, 0 };


    public CustomRandomWalkEQOracle(double restartProbability,
                              long maxSteps,
                              boolean resetStepCount,
                              Random random,
                              SUL<I, O> sul,
                              Collection<? extends String> _alphabet,QueryCache _cache) {
        this(restartProbability, maxSteps, random, sul,_alphabet, _cache);
        this.resetStepCount = resetStepCount;
        this.hc = 0;
        this.alphabet = _alphabet;
        first = true;
        this.happyFlows = new ArrayList<DefaultQuery<I, Word<O>>>();
        this.initHappyFlows();
    }

    public CustomRandomWalkEQOracle(double restartProbability, long maxSteps, Random random, SUL<I, O> sul, Collection<? extends String> _alphabet,QueryCache _cache) {
        this.restartProbability = restartProbability;
        this.maxSteps = maxSteps;
        this.random = random;
        this.sul = sul;
        this.hc = 0;
        this.alphabet = _alphabet;
        first = true;
        this.happyFlows = new ArrayList<DefaultQuery<I, Word<O>>>();
        this.initHappyFlows();
    }

    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                       Collection<? extends I> inputs) {
    	hc++;
    	System.out.println("AAAAAAAAAAAA");
    	return doFindCounterExample(hypothesis, inputs);
        
    }

    private <S, T> DefaultQuery<I, Word<O>> doFindCounterExample(MealyMachine<S, I, T, O> hypothesis,
                                                                 Collection<? extends I> inputs) {
        // reset termination counter?
        if (resetStepCount) {
            steps = 0;
        }

        if (inputs.isEmpty()) {
            return null;
        }

        List<? extends I> choices = CollectionsUtil.randomAccessList(inputs);
        int bound = choices.size();
        S cur = hypothesis.getInitialState();
        WordBuilder<I> wbIn = new WordBuilder<>();
        WordBuilder<O> wbOut = new WordBuilder<>();

        boolean first = true;
        sul.pre();
        try {
            while (steps < maxSteps) {

                if (first) {
                    first = false;
                } else {
                    // restart?
                    double restart = random.nextDouble();
                    if (restart < restartProbability) {
                        sul.post();
                        sul.pre();
                        cur = hypothesis.getInitialState();
                        wbIn.clear();
                        wbOut.clear();
                        first = true;
                    }
                }

                // step
                steps++;
                I in = choices.get(random.nextInt(bound));
                O outSul;

                outSul = sul.step(in);

                T hypTrans = hypothesis.getTransition(cur, in);
                O outHyp = hypothesis.getTransitionOutput(hypTrans);
                wbIn.add(in);
                wbOut.add(outSul);

                // ce?
                if (!outSul.equals(outHyp)) {
                    DefaultQuery<I, Word<O>> ce = new DefaultQuery<>(wbIn.toWord());
                    ce.answer(wbOut.toWord());
                    System.out.println("[CE_"+hc+".size:] " + ce.getInput().size());
                    return ce;
                }
                cur = hypothesis.getSuccessor(cur, in);
            }
            return mightReturnHappyFlow();
//            return null;
        } finally {
            sul.post();
        }
    }
    
    public DefaultQuery<I, Word<O>> mightReturnHappyFlow(){
    	if (happyFlows.size() >0) {
			DefaultQuery<I, Word<O>> d = happyFlows.get(0);
			happyFlows.remove(d);
//			Word<O> w = Word.fromSymbols(sul.step((I) d.getInput()));
//			d.answer(w);
			return d;
		}else{
			return null;
		}
    }
	public void initHappyFlows() {
		ArrayList<String> alphabetAsArray = new ArrayList<String>(this.alphabet);
		for (int[] flow : happyFlowsIndexes) {
			ArrayList<String> happySingleFlow = new ArrayList<String>();
			for (int ind : flow) {
				String symb = alphabetAsArray.get(ind);
				happySingleFlow.add(symb);
				DefaultQuery<I, Word<O>> dq = new DefaultQuery<I, Word<O>>((Word<I>) Word.fromList((List<? extends I>) happySingleFlow.clone()));
				happyFlows.add(dq);
			}
//			DefaultQuery<I, Word<O>> dq = new DefaultQuery<I, Word<O>>((Word<I>) Word.fromList(happySingleFlow));
			// dq.answer("0-OK"); // since it is a happy word
			// dq.answer(properties.get("failureMsg")); // since it is a happy
			// word
//			happyFlows.add(dq);
//			System.out.println("Happy Flows!! " + happyFlows.toString());
		}

	}
    
}