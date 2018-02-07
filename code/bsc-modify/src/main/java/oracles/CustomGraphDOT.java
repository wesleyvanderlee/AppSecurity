package oracles;

import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.commons.util.strings.StringUtil;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.UndirectedGraph;
import net.automatalib.graphs.dot.AggregateDOTHelper;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.graphs.dot.GraphDOT;

/*
	Custom class for printing graphs the way we want them
*/
public abstract class CustomGraphDOT extends GraphDOT{

	
	/**
	 * Renders a {@link Graph} in the GraphVIZ DOT format.
	 * 
	 * @param graph the graph to render
	 * @param dotHelper the helper to use for rendering
	 * @param a the appendable to write to
	 * @throws IOException if writing to <tt>a</tt> fails
	 */
	public static <N,E> void writeRaw(ArrayList<?> inp, Graph<N, E> graph,
			GraphDOTHelper<N,? super E> dotHelper,
			Appendable a) throws IOException {
		
		
		
		if(dotHelper == null)
			dotHelper = new DefaultDOTHelper<N, E>();
		
		boolean directed = true;
		if(graph instanceof UndirectedGraph)
			directed = false;
		
		if(directed)
			a.append("di");
		a.append("graph g {\n");
		

		Map<String,String> props = new HashMap<>();
		
		dotHelper.getGlobalNodeProperties(props);
		if(!props.isEmpty()) {
			a.append('\t').append("node");
			appendParams(props, a);
			a.append(";\n");
		}
		
		props.clear();
		dotHelper.getGlobalEdgeProperties(props);
		if(!props.isEmpty()) {
			a.append('\t').append("edge");
			appendParams(props, a);
			a.append(";\n");
		}
		
		
//		dotHelper.writePreamble(a);
		a.append('\n');
		
		
		MutableMapping<N,String> nodeNames = graph.createStaticNodeMapping();
		
		int i = 0;
		
		for(N node : graph) {
			props.clear();
			if(!dotHelper.getNodeProperties(node, props))
				continue;
			String id = "s" + i++;
			a.append('\t').append(id);
			appendParams(props, a);
			a.append(";\n");
			nodeNames.put(node, id);
		}
		
		for(N node : graph) {
			String srcId = nodeNames.get(node);
			if(srcId == null)
				continue;
			Collection<? extends E> outEdges = graph.getOutgoingEdges(node);
			if(outEdges.isEmpty())
				continue;
			for(E e : outEdges) {
				N tgt = graph.getTarget(e);
				String tgtId = nodeNames.get(tgt);
				if(tgtId == null)
					continue;
				
				if(!directed && tgtId.compareTo(srcId) < 0)
					continue;
				
				props.clear();
				if(!dotHelper.getEdgeProperties(node, e, tgt, props))
					continue;
				
				a.append('\t').append(srcId).append(' ');
				if(directed)
					a.append("-> ");
				else
					a.append("-- ");
				a.append(tgtId);
				appendParamsTransition(inp, props, a);
				a.append(";\n");
			}
		}
		
//		a.append('\n');
//		dotHelper.writePostamble(nodeNames, a);
		a.append("}\n");
		if (a instanceof Flushable) {
			((Flushable) a).flush();
		}
	}
	
	private static void appendParamsTransition(ArrayList<?> inp, Map<String,String> params, Appendable a)
			throws IOException {
		if(params == null || params.isEmpty())
			return;
		a.append(" [");
		boolean first = true;
		for(Map.Entry<String,String> e : params.entrySet()) {
			if(first)
				first = false;
			else
				a.append(' ');
			String key = e.getKey();
			String value = e.getValue();
			if(key.equals("label") && value.contains(" / ")){
				value = convertLabelForGoLang(inp, value);
			}else{
				System.out.println(" I< HERE ABABA "); 
			
			}
			a.append(e.getKey()).append("=");
			// HTML labels have to be enclosed in <> instead of ""
			if(key.equals(GraphDOTHelper.CommonAttrs.LABEL) && value.toUpperCase().startsWith("<HTML>"))
				a.append('<').append(value.substring(6)).append('>');
			else
				StringUtil.enquote(value, a);
		}
		a.append(']');
	}
	
	
	private static void appendParams(Map<String,String> params, Appendable a)
			throws IOException {
		if(params == null || params.isEmpty())
			return;
		a.append(" [");
		boolean first = true;
		for(Map.Entry<String,String> e : params.entrySet()) {
			if(first)
				first = false;
			else
				a.append(' ');
			String key = e.getKey();
			String value = e.getValue();
			a.append(e.getKey()).append("=");
			// HTML labels have to be enclosed in <> instead of ""
			if(key.equals(GraphDOTHelper.CommonAttrs.LABEL) && value.toUpperCase().startsWith("<HTML>"))
				a.append('<').append(value.substring(6)).append('>');
			else
				StringUtil.enquote(e.getValue(), a);
		}
		a.append(']');
	}
	
	private static String convertLabelForGoLang(ArrayList<?> inp, String in){
		String sep = " / ";
        String[] parts = in.split(sep);
		int integerIndex =  inp.indexOf(parts[0]); //value index
		String res = parts[1].split("-")[0];
		
		return integerIndex + sep + res;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Method Called #1 from GraphDOT 
	 */
	@SafeVarargs
	public static <S,I,T> void write(Automaton<S,I,T> automaton,
			Collection<? extends I> inputAlphabet,
			Appendable a, GraphDOTHelper<S,? super TransitionEdge<I,T>> ...additionalHelpers) throws IOException {
		
		write(new ArrayList(inputAlphabet), automaton.transitionGraphView(inputAlphabet), a, additionalHelpers);
	}
	
	/*
	 * Method Called #2 from GraphDOT 
	 */
	@SafeVarargs
	public static <N,E> void write(ArrayList<?> inp, Graph<N, E> graph,
			Appendable a, GraphDOTHelper<N,? super E> ...additionalHelpers) throws IOException {
		GraphDOTHelper<N,? super E> helper = graph.getGraphDOTHelper();
		writeRaw(inp, graph, helper, a, additionalHelpers);
	}
	
	/*
	 * Method Called #3 from GraphDOT 
	 */
	@SafeVarargs
	public static <N,E> void writeRaw(ArrayList<?> inp, Graph<N,E> graph, GraphDOTHelper<N, ? super E> helper, Appendable a, GraphDOTHelper<N, ? super E> ...additionalHelpers) throws IOException {
		List<GraphDOTHelper<N,? super E>> helpers = new ArrayList<>(additionalHelpers.length + 1);
		helpers.add(helper);
		helpers.addAll(Arrays.asList(additionalHelpers));
		
		writeRaw(inp, graph, a, helpers);
	}
	
	/*
	 * Method Called #4 from GraphDOT 
	 */
	public static <N,E> void writeRaw(ArrayList<?> inp, Graph<N,E> graph, Appendable a, List<GraphDOTHelper<N,? super E>> helpers) throws IOException {
		AggregateDOTHelper<N, E> aggHelper = new AggregateDOTHelper<>(helpers);
		writeRaw(inp, graph, aggHelper, a);
	}
	
}
