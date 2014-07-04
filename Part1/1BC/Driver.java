import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class Driver {
    public static void main(String[] args) {
        int numVertices = 500;//20,100,500,1000

        int numTrials = 250;
        double minProbability = 0;
        double maxProbability = 1;
        double probabilityIncrement = .02;
        double numThreads = 4;

        long startTime = System.currentTimeMillis();
        ArrayList<Thread> threadList = new ArrayList<Thread>();
        for(int i = 0; i < numThreads; i++) {
            double computedMin = (minProbability+((i/numThreads)*(maxProbability-minProbability)));
            double computedMax = minProbability+(((i+1)/numThreads)*(maxProbability-minProbability));

            computedMin = computedMin + (probabilityIncrement - computedMin%probabilityIncrement)%probabilityIncrement;
            computedMax = computedMax + (probabilityIncrement - computedMax%probabilityIncrement);

            MyThread thread1 = new MyThread(numVertices,numTrials,computedMin,computedMax,probabilityIncrement);
            thread1.start();
            threadList.add(thread1);
        }
        try {
            for(int i = 0; i < threadList.size();i++){
                threadList.get(i).join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    System.out.println("Time taken: " + (System.currentTimeMillis() - startTime));
    }

}

class MyThread extends Thread {
    private int numVertices;
    private int numTrials;
    private double minProbability;
    private double maxProbability;
    private double probabilityIncrement;
    public MyThread(int numVertices, int numTrials, double minProbability, double maxProbability, double probabilityIncrement) {
        System.out.println("Created new thread with: " + minProbability + " min " + maxProbability + " max.");
        this.numTrials = numTrials;
        this.numVertices = numVertices;
        this.minProbability = minProbability;
        this.maxProbability = maxProbability;
        this.probabilityIncrement = probabilityIncrement;
    }

    public void run() {
        for(double p = minProbability; p < maxProbability; p+=probabilityIncrement) {
            p = round(p,2);
            double runningTotalCostMSTForTrials = 0;
            double runningTotalDiameterMSTForTrials = 0;
            for(int trial = 0; trial<numTrials;trial++) {
                Graph g = Graph.createRandomGraph(numVertices,p);
                double runningTotalCostThisGraph = 0;
                double runningTotalDiameterThisGraph = 0;
                for(int i = 0; i <g.getNumCC();i++) {
                    runningTotalCostThisGraph += g.getWeightMSTForCC(i);
                    runningTotalDiameterThisGraph += g.getDiameterForCC(i);
                }
                runningTotalCostMSTForTrials += runningTotalCostThisGraph/g.getNumCC();
                runningTotalDiameterMSTForTrials += runningTotalDiameterThisGraph/g.getNumCC();
            }
            System.out.println("For p = " + p +
                    " and numVerticies = " + numVertices +
                    " the average cost of MST is " + runningTotalCostMSTForTrials/numTrials +
                    " and the average diameter of an MST is " + runningTotalDiameterMSTForTrials/numTrials);
            //System.out.println(p + "\t" + runningTotalDiameterMSTForTrials/numTrials);
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}





class Graph {
    private double[][] graph;
    private ArrayList<ArrayList<Edge>> edgesByCC = new ArrayList<ArrayList<Edge>>();
    private ArrayList<ArrayList<Edge>> mstByCC = new ArrayList<ArrayList<Edge>>();
    private int[] verticesCC;
    private int numCCs;
    private int[] ccSize;


//  public Graph(int numNodes) {
//    boolean[][] graph = new boolean[numNodes][];
//    for(int i = 0; i < numNodes; i++) {
//      graph[i] = new boolean[i];
//    }
//  }

    public int getNumCC() {
        return numCCs;
    }

    public ArrayList<Edge> getMSTForCC(int cc) {
        return mstByCC.get(cc);
    }

    public double getWeightMSTForCC(int cc) {
        double total = 0;
        for(int i = 0; i<mstByCC.get(cc).size() ;i++) {
            total += mstByCC.get(cc).get(i).weight;
        }
        return total;
    }

    public double getDiameterForCC(int cc) {
        return getDiameter(mstByCC.get(cc));
    }

    public double getDiameter(ArrayList<Edge> edges) {
        double[][] weights = new double[graph.length][graph.length];
        double[][] distances = new double[graph.length][graph.length];
        for(int x = 0; x < weights.length; x++) {
            for(int y = 0; y < weights[x].length; y++) {
                weights[x][y] = -1;
                if(x == y) {
                    weights[x][y] = 0;
                }
            }
        }
        for(Edge e : edges) {
            if(e.weight != 0) {
              weights[e.a][e.b] = 1;
              weights[e.b][e.a] = 1;
            }
        }
        for(int i = 0; i < weights.length; i++) {
            //System.out.println("Exploring " + i);
            recursiveHelper(distances, weights,i,i,0,i);
        }
        double max = 0;
        for(int x = 0; x < distances.length; x++) {
            for(int y = 0; y < distances[x].length; y++) {
                if(distances[x][y]>max) {
                    max = distances[x][y];
                }
            }
        }
        return max;
    }

    private void recursiveHelper(double[][] distances, double[][] weights, int startingFrom, int current, double currentDistance, int previous) {
        distances[current][startingFrom] = currentDistance;
        distances[startingFrom][current] = currentDistance;
        for(int i = 0; i < weights[current].length;i++) {
            if(weights[i][current]!=-1) {
                if(i != previous && i != current) {
                    recursiveHelper(distances, weights, startingFrom, i, currentDistance + weights[current][i], current);
                }
            }
        }
    }

    private void kruskalsAlgorithm(ArrayList<Edge> edges, int cc){
        int curCC = 0;
        Collections.sort(edges);
        TreeMap<Integer,Integer> vertices = new TreeMap<Integer,Integer>();
        for(Edge e : edges) {
            if(vertices.containsKey(e.a) && vertices.containsKey(e.b)) {
                if(!vertices.get(e.a).equals(vertices.get(e.b))) {
                    int toDelete = vertices.get(e.b);
                    for(Integer i : vertices.keySet()) {
                        if(vertices.get(i).equals(toDelete)) {
                            vertices.put(i,vertices.get(e.a));
                        }
                    }
                }
                else {
                    continue;
                }
            }
            else if(vertices.containsKey(e.a)) {
                vertices.put(e.b, vertices.get(e.a));
            }
            else if(vertices.containsKey(e.b)) {
                vertices.put(e.a, vertices.get(e.b));
            }
            else {
                vertices.put(e.a,curCC);
                vertices.put(e.b, curCC);
                curCC++;
            }
            mstByCC.get(cc).add(e);

            if(mstByCC.get(cc).size() == ccSize[cc] - 1) {
                break;
            }

        }
    }

    public Graph(double[][] graph) {
        this.graph = graph;
        verticesCC = new int[graph.length];
        depthFirstSearch();
        fillEdgesByCC();
        ccSize = new int[numCCs];
        for(int i = 0; i < verticesCC.length; i++) {
            ccSize[verticesCC[i]]++;
        }
        for(int i = 0; i < numCCs; i++) {
            mstByCC.add(new ArrayList<Edge>());
            kruskalsAlgorithm(edgesByCC.get(i), i);
        }
    }

    public static Graph createRandomGraph(int numNodes, double probability) {
        double[][] inputGraph = new double[numNodes][];
        for(int i = 0; i < numNodes; i++) {
            inputGraph[i] = new double[i];
        }
        for(int x= 0; x < numNodes; x++) {
            for(int y = 0; y < inputGraph[x].length; y++) {
                inputGraph[x][y] = (Math.random() < probability ? Math.random() : 0);
            }
        }
        return new Graph(inputGraph);
    }

    private void depthFirstSearch() {
        boolean[] visited = new boolean[graph.length];
        for(int i = graph.length - 1; i >= 0; i--) {
            if(!visited[i]) {
                numCCs++;
                visitEachAdjoining(i,visited, numCCs-1);
            }
        }
    }

    private void visitEachAdjoining(int i, boolean[] visited, int cc) {
        visited[i] = true;
        verticesCC[i] = cc;
        for(int x = graph[i].length - 1; x >= 0 ; x-- ) {
            if(!visited[x]) {
                if(graph[i][x]!=0) {
                    visitEachAdjoining(x, visited, cc);
                }
            }
        }
        for(int x = i+1; x < graph.length; x++) {
            if(!visited[x]) {
                if(graph[x][i]!=0) {
                    visitEachAdjoining(x, visited, cc);
                }
            }
        }
    }

    private void fillEdgesByCC() {
        for(int i = 0; i<numCCs;i++) {
            this.edgesByCC.add(new ArrayList<Edge>());
        }
        for(int x = 0; x < graph.length; x++) {
            for(int y = 0; y < graph[x].length; y++) {
                if(graph[x][y]!=0) {
                    Edge e = new Edge(x,y,graph[x][y]);
                    edgesByCC.get(verticesCC[x]).add(e);
                }
            }
        }
    }

    public String toString() {
        String s = "Graph:\n";
        for(int x = 0; x < graph.length; x++) {
            for(int y = 0; y < graph[x].length; y++) {
                if(graph[x][y]!=0) {
                    s+= "From " + x + " to " + y + " with weight " + graph[x][y] + "\n";
                }
            }
        }
        s += "End Graph";
        return s;
    }
}




class Edge implements Comparable<Edge>{
    public final int a,b;
    public final double weight;
    public Edge(int a, int b, double weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }
    public String toString() {
        return "From " + a + " to " + b + " with weight " + weight;
    }

    @Override
    public int compareTo(Edge o) {
        return weight - o.weight < 0 ? -1 : (weight-o.weight > 0 ? 1 : 0);
    }
}
