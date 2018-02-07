package oracles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.bunq.util.Property;
import com.google.common.collect.Iterables;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class WMethodEQOracle1<A extends UniversalDeterministicAutomaton<Integer, I, String, String, String> & Output<I, D>, I, D>
		implements EquivalenceOracle<A, I, D> {

	private int maxDepth;
	private final MembershipOracle<I, D> sulOracle;
	private Property properties;
	private int hypCount;
	private final String hyploc = "hyp/";
	private List<Query> black;

	/**
	 * Constructor.
	 *
	 * @param maxDepth
	 *            the maximum length of the "middle" part of the test cases
	 * @param sulOracle
	 *            interface to the system under learning
	 */
	public WMethodEQOracle1(int maxDepth, MembershipOracle<I, D> sulOracle) {
		this.maxDepth = maxDepth;
		this.sulOracle = sulOracle;
		this.properties = Property.getInstance();
		this.hypCount = 0;
		black = new ArrayList<Query>();
	}

	@Override
	public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
		System.out.println("In findCounterExample of WMethod1");

		//////////
		List<Word<I>> transCover = Automata.transitionCover(hypothesis, inputs);
		Iterable<Word<I>> middleTuples = Iterables.transform(CollectionsUtil.allTuples(inputs, 0, maxDepth),
				Word::fromList);
		List<Word<I>> characterizingSet = Automata.characterizingSet(hypothesis, inputs);

		// Special case: List of characterizing suffixes may be empty,
		// but in this case we still need to test!
		if (characterizingSet.isEmpty()) {
			characterizingSet = Collections.singletonList(Word.epsilon());
		}

		// Iterator<List<Word<I>>> wMethodIter =
		// CollectionsUtil.allCombinations(transCover, middleTuples,
		// characterizingSet).iterator();
		// Iterable<List<Word<I>>> i =
		// CollectionsUtil.allCombinations(transCover, middleTuples,
		// characterizingSet);

		// Iterator<List<Word<I>>> wMethodIter =
		// (Iterator<List<Word<I>>>) CollectionsUtil.allCombinations(transCover,
		// middleTuples, characterizingSet);

		WordBuilder<I> wb = new WordBuilder<>();
		System.out.println("CE " + transCover.size() + " " + maxDepth + " " + characterizingSet.size());

		for (Word<I> trans : transCover) {
			for (Word<I> middle : middleTuples) {
				for (Word<I> suffix : characterizingSet) {
					System.out.println(transCover.indexOf(trans) + " " +
					 middle.size() + " " +characterizingSet.indexOf(suffix));

					wb.append(trans).append(middle).append(suffix);
					DefaultQuery<I, D> query = new DefaultQuery<>(wb.toWord());
					wb.clear();
					D hypOutput = hypothesis.computeOutput(query.getInput());
					sulOracle.processQueries(Collections.singleton(query));
					if (!Objects.equals(hypOutput, query.getOutput())) {
						hypCount++;
						return query;
					}

				}
			}
		}

		// No hypcount increasing, as at this point the hypothesis is equivalent
		// to the sut
		return null;
	}

	// @Override
	public DefaultQuery<I, D> findCounterExampleOld(A hypothesis, Collection<? extends I> inputs) {
		System.out.println("In findCounterExample of WMethod");

		List<Word<I>> transCover = Automata.transitionCover(hypothesis, inputs);
		List<Word<I>> charSuffixes = Automata.characterizingSet(hypothesis, inputs);
		System.out.println(transCover.size());
		System.out.println(charSuffixes.size());

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

}