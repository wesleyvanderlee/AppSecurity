package com.wesley.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphReader {
	private final String graphLocation = "graphs/";
	String filename;
	private List<Vertex> nodes;
	private List<Edge> edges;
	int nodeCounter = 0;
	int laneCounter = 0;

	public GraphReader(String _filename) {
		nodes = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		File f = new File(_filename);
		if(f.exists() && f.isFile()){
			this.filename = _filename;
		}else{
			File _f = new File(graphLocation);
			File[] files = _f.listFiles();
			this.filename = selectFile(files);
			System.out.println("[i]\tDid not find file: " + _filename + ". Selecting: " + filename + "\n");
		}
	}
	
	public String selectFile(File[] files){
		String fileNameMax = "";
		for (File f : files){
			if(f.getName().compareTo(fileNameMax) > 0){
				fileNameMax = f.getName() ;
			}
		}
		return fileNameMax;
	}

	public Graph read() {
		String line = null;
		try {
			FileReader fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				processLine(line);
			}

			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" +  filename + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + filename + "'");
		}

		EnrichedGraph graph = new EnrichedGraph(nodes, edges);
		return graph;
	}

	public void processLine(String line) {

		if (line.contains("\ts")) { // It is content

			if (line.contains("->")) { // line = edge
				String[] labelparts = line.split("\"");
				String label = labelparts[1];

				String[] parts = labelparts[0].split(" ");
				int source = Integer.parseInt(parts[0].substring(2));
				int destin = Integer.parseInt(parts[2].substring(1));

				Edge lane = new Edge(laneCounter, nodes.get(source), nodes.get(destin), 1, label);
				edges.add(lane);

				laneCounter++;
			} else { // line = state indicator
				String state = line.split(" ")[0];
				if (!state.contains("s")) {
					System.out.println("Problem in GraphReader 0x01");
					System.exit(0);
				}

				Vertex location = new Vertex(state.substring(2), "Node_" + state.substring(2));
				nodes.add(location);
				nodeCounter++;
			}

		}
	}

	/*
	 * Example: digraph g { __start0 [label="" shape="none"];
	 * 
	 * s0 [shape="circle" label="0"]; s1 [shape="circle" label="1"]; s2
	 * [shape="circle" label="2"]; s0 -> s1
	 * [label="push-row_button_To- .activities.LocationPickerPlanner"]; s0 -> s0
	 * [label="push-planJourney_button- .activities.StartupActivity"]; s0 -> s2
	 * [label="back% / com.android.launcher2.Launcher"]; s2 -> s2
	 * [label="back% / com.android.launcher2.Launcher"];
	 * 
	 * __start0 -> s0; }
	 * 
	 */
}
