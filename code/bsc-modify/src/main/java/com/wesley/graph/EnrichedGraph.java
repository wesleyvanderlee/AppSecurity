package com.wesley.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bunq.apk.apkinfo;
import com.bunq.teacher.Action;
import com.bunq.teacher.AndroidInstrumentator;
import com.wesley.emulator.AndroidEmulatorInstrumentator;

public class EnrichedGraph extends Graph {

	public EnrichedGraph(List<Vertex> vertexes, List<Edge> edges) {
		super(vertexes, edges);

	}

	/*
	 * REQUIRES THE EMULATOR TO BE STARTED!!
	 */
	public void enrich(AndroidInstrumentator instrumentator,AndroidEmulatorInstrumentator aei, apkinfo apkinfo) {
		System.out.println("[i]\tGraph enrichment started");
		System.out.print("\t\tTraversing vertexes:\n");
		for (int j = 0; j < this.vertexes.size(); j++)
			System.out.print(".");
		System.out.println();
		System.out.print("\t\t\t");

		for (Vertex v : this.vertexes) {
			System.out.print(".");
			int state = Integer.parseInt(v.getId());
			List<Edge> accessSequence = this.getAccessSequence(state);
			instrumentator.reset("pl_hard_reset");
			// #First go to the state
			gotoState(accessSequence, instrumentator);

			// # Enriching 1: Activity name for this state
			String stateActivity = instrumentator.getCurrentActivity();
			String fullActivity = apkinfo.getPublicAPKName() + stateActivity;
			v.setActivity(fullActivity);

			// # Enriching 2: Screentext for this state
			List<String> screenText = instrumentator.getTextOnScreen();
			v.addScreenText(screenText);
		}
		
		
		System.out.print("\t\tTraversing edges:\n");
		for (int j = 0; j < this.getEdgesOK().size(); j++)
			System.out.print(".");
		System.out.println();
		System.out.print("\t\t\t");

		// Retrieve web requests per transition
		for (Edge e : this.getEdgesOK()) {
			System.out.println("newE");
			System.out.println(e.toStringGrande());
			System.out.print(".");
			int preState = Integer.parseInt(e.getSource().getId());
			List<Edge> accessSequence = this.getAccessSequence(preState);
			System.out.println("AS Determined");
			aei.reset("pl_hard_reset");
			System.out.println("Ins is reset. going to prestate");
			// #First go to the state
			gotoState(accessSequence, aei);
			System.out.println("gotoState is finished");

			// Start proxy;
			List<String> commandLine = new ArrayList<String>();
			commandLine.add("scripts/mitmdump");
			commandLine.add("-s scripts/customScript.py");
			commandLine.add("-p 8889");
			ProcessBuilder builder = new ProcessBuilder(commandLine);
			builder.redirectErrorStream(true);
			builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			System.out.println("Entering try");
			try {
				// Start proxy
				Process process = builder.start();
				System.out.println("after start");
				// Perform step
				try{
				Action action = Action.parse(e.getLabelAction());
				action.dispatch(aei);
				System.out.println("sleeping");

				Thread.sleep(20000);
				}catch(Exception d){
					d.printStackTrace();
				}
				// Wait for simulation

				System.out.println("awake");
				// Destroy process
				process.destroy();
				System.out.println("destroyed and reading file");
				// According to customScript.py, the output will be in the file
				// requests.txt

				// > Read and delete file requests.txt
				List<String> requests = readRequestFile();
				System.out.println("Edge: " + e.getId() + requests.toString());
				
				System.out.println(requests);

			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}

			// Close proxy
			// System.out.println(action);

		}
	}

	public List<String> readRequestFile() {
		List<String> requests = new ArrayList<String>();
		File f = new File ("requests.txt");
		if(!f.exists()){
			System.out.println("ERRRRRRRRRRORRRRRRRRRRRRRRR F DOES NOT EXIST");
		}
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader("requests.txt");
			br = new BufferedReader(fr);
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				requests.add(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		f.delete();
		
		return requests;
	}

	public void gotoState(List<Edge> sequence, AndroidInstrumentator instrumentator) {
		if (sequence == null) // first state or already there
			return;
		for (Edge e : sequence) {
			try {
				// Action action = Action.parse(e.getLabel().split(" / ")[0]);
				// System.out.println(e.getLabel().split(" / ")[0]);
				Action action = Action.parse(e.getLabelAction());

				// System.out.println(action);
				action.dispatch(instrumentator);
				Thread.sleep(1000);
			} catch (Exception exception) {
				System.out.println(exception);
			}
		}
	}
	
	public void gotoState(List<Edge> sequence, AndroidEmulatorInstrumentator instrumentator) {
		if (sequence == null) // first state or already there
			return;
		for (Edge e : sequence) {
			try {
				// Action action = Action.parse(e.getLabel().split(" / ")[0]);
				// System.out.println(e.getLabel().split(" / ")[0]);
				Action action = Action.parse(e.getLabelAction());

				// System.out.println(action);
				action.dispatch(instrumentator);
				Thread.sleep(1000);
			} catch (Exception exception) {
				System.out.println(exception);
			}
		}
	}

}

//
//
// this.setIdentified(0);
// ArrayList<String> callableActivities =
// apkinfo.getPublicAPK().getCallableActivitiesMinusStart();
// if (callableActivities.isEmpty()) {
// return;
// }
// for (Vertex v : graph.getVertexes()) {
//
// String stateActivity = instrumentator.getCurrentActivity();
// String fullActivity = apkinfo.getPublicAPKName() + stateActivity;
// if (callableActivities.contains(fullActivity)) {
// callableActivities.remove(fullActivity);
// }
// }
// // All states are checked
// if (!callableActivities.isEmpty()) {
// findings.add("The following activities are callable but not modeled: " +
// callableActivities.toString());
// this.setIdentified(1); // Likely vulnerability is present
// }