package com.wesley.graph;

import java.util.List;

public class Init {

	public static void main(String[] args) {
		/*
		 * GraphRunner gr = new GraphRunner(); gr.testExcute();
		 */
		Graph gr = (new GraphReader("graph.txt")).read();
		List<Edge> le = gr.getAccessSequence(3);	//Can return null ico no path
		for(Edge e : le){
			System.out.print(e.getSource() + "\t");
			System.out.println(e.getLabel());
		}
	}
}
