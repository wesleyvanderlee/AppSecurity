package com.wesley.graph;




import java.util.ArrayList;
import java.util.List;



/*

    Class for testing purpose of custom graph datatype
*/
public class GraphRunner {

    private List<Vertex> nodes;
    private List<Edge> edges;
    
    public GraphRunner(){
    	 nodes = new ArrayList<Vertex>();
         edges = new ArrayList<Edge>();
    }

    public void testExcute() {
       
        for (int i = 0; i < 11; i++) {
            Vertex location = new Vertex("Node_" + i, "Node_" + i);
            nodes.add(location);
        }

        addLane("ROT-AMS", 0, 1, 2);
        addLane("ROT-UTR", 0, 2, 1);
        addLane("ROT-BRE", 0, 7, 1);
        addLane("AMS-UTR", 1, 2, 1);
        addLane("UTR-BRE", 2, 7, 3);
        addLane("UTR-AFO", 2, 5, 1);
        addLane("UTR-GRO", 2, 4, 3);
        addLane("BRE-LIM", 7, 3, 4);
        addLane("AFO-ARN", 5, 6, 2);
        addLane("GRO-NPO1", 6, 8, 1000);
        addLane("GRO-NPO2", 6, 9, 1000);
        addLane("GRO-NPO3", 6, 10, 1000);
        
        addLane("ROT-AMSVV", 1,0, 2);
        addLane("ROT-UTRVV", 2, 0, 1);
        addLane("ROT-BREVV", 7, 0, 1);
        addLane("AMS-UTRVV", 2, 1, 1);
        addLane("AMS-GROVV", 4, 1, 8);
        addLane("UTR-BREVV", 7, 2, 3);
        addLane("UTR-AFOVV", 5, 2, 1);
        addLane("UTR-GROVV", 4, 2, 3);
        addLane("BRE-LIMVV", 3, 7, 4);
        addLane("AFO-ARNVV", 6, 5, 2);
        addLane("GRO-NPOVV1", 8, 6, 1000);
        addLane("GRO-NPO2VV", 9, 6, 1000);
        addLane("GRO-NPO3VV", 10, 6, 1000);

        // Lets check from location Loc_1 to Loc_10
        Graph graph = new Graph(nodes, edges);
        Dijkstra dijkstra = new Dijkstra(graph);
        dijkstra.execute(nodes.get(3));
        List<Vertex> path = dijkstra.getPath(nodes.get(4));

        System.out.println(path);

        for (Vertex vertex : path) {
            System.out.println(vertex);
        }

    }

    private void addLane(String laneId, int sourceLocNo, int destLocNo,
            int duration) {
        Edge lane = new Edge(laneId,nodes.get(sourceLocNo), nodes.get(destLocNo), duration, "");
        edges.add(lane);
    }
}
