package com.wesley.graph;

import com.bunq.util.Property;

/*
 * RETRIEVED FROM: http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
 */

public class Edge {
	/*
	 * New attributes should be ensured in the copy
	 */
	private final String id;
	private final Vertex source;
	private final Vertex destination;
	private final int weight;
	private final String label;

	
	public String visualization(){
		return String.format(" { \"id\":%s, \"source\" : %s , \"target\":%s, \"label\":\"%s\"}",id,source.getId(),destination.getId(),getPrettyLabel());
	
	}
	public String getPrettyLabel(){
		Property properties = Property.getInstance();
		String[] ret = this.label.split(properties.get("appPackage") + ":id");
		String r =ret[ret.length -1].replaceAll("\"", "'");
		
		if(this.label.contains("enterText")){
			String[] args = this.label.split("#");
			String argument = args[args.length -1].split(" / ")[0];
			r += "#" + argument;
		}
		
		return r;
	}


	
	public Edge(String id, Vertex source, Vertex destination, int weight, String label) {
		this.id = id;
		this.source = source;
		this.destination = destination;
		this.weight = weight;
		this.label = label;
	}

	public Edge(int id, Vertex source, Vertex destination, int weight, String label) {
		this.id = id + "";
		this.source = source;
		this.destination = destination;
		this.weight = weight;
		this.label = label;
	}

	public Edge copy(){
		Edge e = new Edge(this.id,this.source,this.destination,this.weight,this.label);
		return e;
	}
	
	public String getId() {
		return id;
	}

	public Vertex getDestination() {
		return destination;
	}

	public Vertex getSource() {
		return source;
	}

	public int getWeight() {
		return weight;
	}

	public String getLabel() {
		return label;
	}

	public String getLabelAction() {
		return label.replace(" / 0-OK", "");

	}

	@Override
	public String toString() {
		return source + " " + destination;
	}
	
	public String toStringGrande() {
		return source + " " + destination + " " +label;
	}

}
