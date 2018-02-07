package com.wesley.graph;
/*
 * RETRIEVED AND MODIFIED FROM: http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
 */

import java.util.ArrayList;
import java.util.List;

public class Graph {
	protected List<Vertex> vertexes;
	protected List<Edge> edges;

	public Graph(List<Vertex> vertexes, List<Edge> edges) {
		this.vertexes = vertexes;
		this.edges = edges;
	}

	public List<Vertex> getVertexes() {
		return vertexes;
	}

	public List<Edge> getEdges() {
		return edges;
	}
	
	public List<Edge> getEdgesOK() {
		List res = new ArrayList<Edge>();
		for(Edge e : edges){
			if(!e.getLabel().contains("NOTFOUND")){
				res.add(e);
			}
		}
		return res;
	}

	public List<Vertex> getLeafs() {
		List<Vertex> list = new ArrayList<Vertex>();
		for (Vertex v : this.vertexes) {
			if (this.getEdgeForSource(v) == null) {
				list.add(v);
			}
		}
		return list;
	}

	public Edge getEdgeForSource(Vertex v) {
		for (Edge e : this.edges) {
			if (e.getSource().equals(v) && !e.getLabel().contains("NOTFOUND")) {
				// System.out.println("Should filter edges for notfound by
				// computing leafs. Edge:" + e.getLabel());
				return e;
			}
		}
		return null;
	}

	public List<Edge> getEdgesForSource(Vertex v) {
		List<Edge> list = new ArrayList<Edge>();
		for (Edge e : this.edges) {
			if (e.getSource().equals(v)) {
				list.add(e);
			}
		}
		return list;
	}

	public List<Edge> getAccessSequence(int i) {
		try {
			if (i >= this.vertexes.size() || i == 0)
				return null;

			List<Edge> pathedges = new ArrayList<Edge>();
			List<Vertex> path = this.dijkstra(i);

			for (Vertex v : path) {
				int curIndex = path.indexOf(v);
				if (curIndex < path.size() - 1) {
					Edge e = getEdgeDirectlyBetween(v, path.get(curIndex + 1));
					pathedges.add(e);
				}
			}
			return (pathedges.size() > 0) ? pathedges : null;
		} catch (Exception e) {
			return null;
		}
	}

	public List<Vertex> getAccessSequenceVertexes(int i) {
		try {
			if (i >= this.vertexes.size() || i == 0)
				return null;

			List<Vertex> path = this.dijkstra(i);

			return (path.size() > 0) ? path : null;
		} catch (Exception e) {
			return null;
		}
	}

	private List<Vertex> dijkstra(int i) {
		Dijkstra dijkstra = new Dijkstra(this);
		dijkstra.execute(this.getVertexes().get(0));
		ArrayList<Vertex> path = dijkstra.getPath(this.getVertexes().get(i));
		return path;
	}

	private Edge getEdgeDirectlyBetween(Vertex v1, Vertex v2) {
		for (Edge e : this.edges) {
			if (e.getSource().equals(v1) && e.getDestination().equals(v2))
				return e;
		}
		return null;
	}

	public Graph copy() {
		List<Vertex> newVertexes = new ArrayList<Vertex>();
		for (Vertex v : this.vertexes) {
			newVertexes.add(v.copy());
		}
		List<Edge> newEdges = new ArrayList<Edge>();
		for (Edge e : this.edges) {
			newEdges.add(e.copy());
		}
		return new Graph(newVertexes, newEdges);
	}

	public void removeVertex(Vertex vertexToBeRemoved) {
		List<Vertex> newV = new ArrayList<Vertex>();
		for (Vertex v : this.vertexes) {
			if (!v.equals(vertexToBeRemoved)) {
				// this.vertexes.remove(v);
				newV.add(v);
			}
		}
		List<Edge> newE = new ArrayList<Edge>();
		for (Edge e : this.edges) {
			if (!e.getSource().equals(vertexToBeRemoved) && !e.getDestination().equals(vertexToBeRemoved)) {
				newE.add(e);
			}
		}
		this.vertexes = newV;
		this.edges = newE;
	}

}
