package com.wesley.graph;

public class ReferenceGraphReader extends GraphReader{
	private final String graphLocation = ".";

	public ReferenceGraphReader(){
		super("ReferenceGraph.dot");
	}
	
}
