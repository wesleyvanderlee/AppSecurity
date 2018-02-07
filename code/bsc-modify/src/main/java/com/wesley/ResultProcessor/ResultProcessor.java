package com.wesley.ResultProcessor;

import java.util.ArrayList;
import java.util.List;

import com.bunq.apk.apkinfo;
import com.bunq.teacher.AndroidInstrumentator;
import com.wesley.VulnerabilityAlgorithm.AuthenticationBypass;
import com.wesley.VulnerabilityAlgorithm.ImproperPlatformUsage;
import com.wesley.VulnerabilityAlgorithm.VulnerabilityIdentificationAlgorithm;
import com.wesley.emulator.AndroidEmulatorInstrumentator;
import com.wesley.graph.EnrichedGraph;
import com.wesley.graph.EnrichedGraphWriter;
import com.wesley.graph.Graph;
import com.wesley.graph.GraphReader;
import com.wesley.graph.Vertex;

public class ResultProcessor {
	ArrayList<String> findings;

	private apkinfo apkinfo;
	private Graph graph;

	AndroidInstrumentator instrumentator;

	public ResultProcessor(String stamp,AndroidEmulatorInstrumentator aei, apkinfo _apkinfo) {
		this.apkinfo = _apkinfo;
		this.instrumentator = new AndroidInstrumentator();
		this.findings = new ArrayList<String>();
		instrumentator.startApp();
		this.graph = (new GraphReader(stamp)).read();
		((EnrichedGraph) this.graph).enrich(this.instrumentator,aei,this.apkinfo);
		
		EnrichedGraphWriter egr = new EnrichedGraphWriter();
		egr.write((EnrichedGraph) this.graph);
		this.setupAlgorithms();
	}

	private ArrayList<VulnerabilityIdentificationAlgorithm> algs;
	
	public void setupAlgorithms(){
		this.algs = new ArrayList<VulnerabilityIdentificationAlgorithm>();
		
		this.algs.add(new ImproperPlatformUsage());
		this.algs.add(new DeadEnds());
		this.algs.add(new AuthenticationBypass());
	}
	
	public List<Vertex> getLeafs(){
		List<Vertex> leafs = this.graph.getLeafs();
		if (!leafs.isEmpty()) {
			findings.add("The following activities are callable but not modeled: " + leafs.toString());
		}
		return leafs;
	}
	
	public void run(){
		for(VulnerabilityIdentificationAlgorithm a : algs){
//			a.identify(graph, instrumentator, apkinfo);
			a.identify(this.graph, this.apkinfo);

		}
		
	}

	public void print(){
		String res = VulnerabilityIdentificationAlgorithm.getReportingHeader();
		for(VulnerabilityIdentificationAlgorithm a : algs){
			res += ("\n"+ a.ReportingToString());
		}
		System.out.println(res);
	}
	
	public void printFindings() {
		System.out.println("\n\n***********************************");
		System.out.println("**                               **");
		System.out.println("**     -- FINDING REPORTS --     **");
		System.out.println("**                               **");
		System.out.println("***********************************");
		if (findings.size() == 0) {
			System.out.println("**\n**\n**\n**");
			System.out.println("**  There were no findings");
		} else {
			System.out.println("**  There are " + findings.size() + " findings:");
			for (String finding : findings)
				System.out.println("**\t- " + finding);
		}
		System.out.println("**");
		System.out.println("**     -- END OF REPORT --       ");
		System.out.println("***********************************");

	}
	/*
	 * private ArrayList<String> getAccessSequence(Integer state,
	 * ArrayList<String> accessStringIn, int maxDepth) { if ((new
	 * Integer(0).equals(state))) return new ArrayList<String>(); if (maxDepth
	 * == 0) return null; ArrayList<String> accessString = new
	 * ArrayList<String>(); accessString.addAll(accessStringIn); for (int i = 0;
	 * i < alphabet.size(); i++) { accessString.add(alphabet.getSymbol(i)); if
	 * (state.equals(hypothesisMachine.getState(accessString))) { return
	 * accessString; } accessString.remove(accessString.size() - 1); } for (int
	 * i = 0; i < alphabet.size(); i++) {
	 * accessString.add(alphabet.getSymbol(i)); ArrayList<String>
	 * recursiveAccessString = getAccessSequence(state, accessString, maxDepth -
	 * 1); if (recursiveAccessString != null &&
	 * state.equals(hypothesisMachine.getState(recursiveAccessString))) { return
	 * recursiveAccessString; } accessString.remove(accessString.size() - 1); }
	 * return null; }
	 * 
	 * private ArrayList<String> getAccessSequence(Integer state) {
	 * ArrayList<String> res = this.getAccessSequence(state, new
	 * ArrayList<String>(), 7); if (res == null) { res = new
	 * ArrayList<String>(); } return res; }
	 * 
	 * private ArrayList<String> allActivitiesReachedReal() { // return
	 * action.dispatch(instrumentator); ArrayList<String> callableActivities =
	 * this.apkinfo.getPublicAPK().getCallableActivities(); for (Integer state :
	 * hypothesisMachine.getStates()) { if (callableActivities.isEmpty()) {
	 * return callableActivities; }
	 * 
	 * ArrayList<String> accessSequence = getAccessSequence(state);
	 * instrumentator.reset("hard_reset"); for (String step : accessSequence) {
	 * try { Action action = Action.parse(step);
	 * action.dispatch(instrumentator); Thread.sleep(1000); } catch (Exception
	 * e) { } } String stateActivity = instrumentator.getCurrentActivity();
	 * String fullActivity = this.apkinfo.getPublicAPKName() + stateActivity; if
	 * (callableActivities.contains(fullActivity)) {
	 * callableActivities.remove(fullActivity); } }
	 * 
	 * if (!callableActivities.isEmpty()) {
	 * findings.add("The following activities are callable but not modeled: " +
	 * callableActivities.toString()); }
	 * 
	 * return callableActivities;
	 * 
	 * }
	 * 
	 * private ArrayList<String> allActivitiesReached() { // return
	 * action.dispatch(instrumentator); ArrayList<String> callableActivities =
	 * this.apkinfo.getPublicAPK().getCallableActivities(); for (Integer state :
	 * hypothesisMachine.getStates()) { if (callableActivities.isEmpty()) {
	 * return callableActivities; }
	 * 
	 * ArrayList<String> accessSequence = getAccessSequence(state);
	 * instrumentator.reset("hard_reset"); for (String step : accessSequence) {
	 * try { Action action = Action.parse(step);
	 * action.dispatch(instrumentator); Thread.sleep(1000); } catch (Exception
	 * e) { } } String stateActivity = instrumentator.getCurrentActivity();
	 * String fullActivity = this.apkinfo.getPublicAPKName() + stateActivity; if
	 * (callableActivities.contains(fullActivity)) {
	 * callableActivities.remove(fullActivity); } }
	 * 
	 * if (!callableActivities.isEmpty()) {
	 * findings.add("The following activities are callable but not modeled: " +
	 * callableActivities.toString()); }
	 * 
	 * return callableActivities;
	 * 
	 * }
	 * 
	 */
/*
	private ArrayList<String> allActivitiesReached() {
		ArrayList<String> callableActivities = this.apkinfo.getPublicAPK().getCallableActivities();
		for (Vertex v : this.graph.getVertexes()) {
			if (callableActivities.isEmpty()) {
				return callableActivities;
			}
			int state = Integer.parseInt(v.getId());
			if (state == 0)
				continue;
			List<Edge> accessSequence = this.graph.getAccessSequence(state);
			instrumentator.reset("hard_reset");
			for (Edge e : accessSequence) {
				try {
					Action action = Action.parse(e.getLabel());
					action.dispatch(instrumentator);
					Thread.sleep(1000);
				} catch (Exception exception) {
				}
			}
			String stateActivity = instrumentator.getCurrentActivity();
			String fullActivity = this.apkinfo.getPublicAPKName() + stateActivity;
			if (callableActivities.contains(fullActivity)) {
				callableActivities.remove(fullActivity);
			}
		}
		// All states are checked
		if (!callableActivities.isEmpty()) {
			findings.add("The following activities are callable but not modeled: " + callableActivities.toString());
		}
		return callableActivities;

	}*/
}
