import java.util.*;

class Edge {
    String to;
    double travelTime;

    public Edge(String to, double travelTime) {
        this.to = to;
        this.travelTime = travelTime;
    }
}

public class Graph {
     Map<String, List<Edge>> adjList = new HashMap<>();

    public void addEdge(String from, String to, double travelTime) {
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.get(from).add(new Edge(to, travelTime));

        adjList.putIfAbsent(to, new ArrayList<>());
    }

    public  List<Edge> getNeighbors(String node) {
        return adjList.getOrDefault(node, new ArrayList<>());
    }

    public Set<String> getNodes() {
        return adjList.keySet();
    }
}
