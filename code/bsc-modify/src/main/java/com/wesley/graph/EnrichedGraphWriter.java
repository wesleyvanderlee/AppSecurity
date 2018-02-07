package com.wesley.graph;

import java.io.PrintWriter;
import java.util.List;

public class EnrichedGraphWriter {
	public EnrichedGraphWriter(){
		
	}
	
	public void write(EnrichedGraph graph){
		PrintWriter writer;
		try {
			writer = new PrintWriter("visualize/graph.json", "UTF-8");
			writer.println("{");
			writer.println("\"nodes\":[");
			List<Vertex> vertexes =graph.getVertexes(); 
			for(Vertex v : vertexes){
				writer.println(v.visualization());
				if(vertexes.indexOf(v) != vertexes.size() -1){
					writer.print(",");
				}
				writer.println();
			}
			writer.println("],");
			writer.println("\"links\":[");
			List<Edge> edges = graph.getEdgesOK(); 
			for(Edge e : edges){
				writer.println(e.visualization());
				if(edges.indexOf(e) != edges.size() -1){
					writer.print(",");
				}
				writer.println();
			}
			writer.println(" ] ");
			writer.println(" } ");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
}
