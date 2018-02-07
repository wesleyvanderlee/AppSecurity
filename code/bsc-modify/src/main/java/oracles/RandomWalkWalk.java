//https://github.com/LearnLib/learnlib/blob/develop/oracles/equivalence-oracles/src/main/java/de/learnlib/oracle/equivalence/mealy/RandomWalkEQOracle.java
package oracles;

import java.util.Collection;
import java.util.List;
import java.util.Random;

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
public class RandomWalkWalk<I, O> implements MealyEquivalenceOracle<I, O> {

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

    public RandomWalkWalk(double restartProbability,
                              long maxSteps,
                              boolean resetStepCount,
                              Random random,
                              SUL<I, O> sul) {
        this(restartProbability, maxSteps, random, sul);
        this.resetStepCount = resetStepCount;
        this.hc = 0;
    }

    public RandomWalkWalk(double restartProbability, long maxSteps, Random random, SUL<I, O> sul) {
        this.restartProbability = restartProbability;
        this.maxSteps = maxSteps;
        this.random = random;
        this.sul = sul;
        this.hc = 0;
    }

    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                       Collection<? extends I> inputs) {
    	hc++;
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
            return null;
        } finally {
            sul.post();
        }
    }
}