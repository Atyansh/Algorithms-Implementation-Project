public class Driver {
    public static void main(String[] args) {
        
        if(args.length != 3) {
            System.out.println("Incorrect arguments. Please run by \"java " +
                               "Driver <numNodes> <numTrials> <1 for meanCC," +
                               " 2 for stdDev, 3 for runTime>\"");
            System.exit(-1);
        }
        

        int numNodes = Integer.parseInt(args[0]);
        int numTrials = Integer.parseInt(args[1]);
        int choice = Integer.parseInt(args[2]);

        if(choice < 1 || choice > 3) {
            System.out.println("Incorrect choice. Pick 1 for meanCC, 2 for" + 
                               " stdDev, 3 for runTime");
            System.exit(-1);
        }

        double yAxis = 0;

        long start, time;

        for(double p = 0; p <= 1; p += .02) {
            start = System.nanoTime();
            
            int totalNum = 0;
            int[] stdDevCompute = new int[numTrials];
            
            for(int i = 0; i < numTrials; i++) {
                int numCC = Graph.createRandomGraph(numNodes,p).getNumCC();
                totalNum += numCC;
                stdDevCompute[i] = numCC;
            
            }
            
            time = System.nanoTime() - start;
            
            double mean = ((double)totalNum)/numTrials;
            int differenceSquaredRunning = 0;
            
            for(int i = 0; i < numTrials; i++) {
                int differenceSquared = (int)Math.pow(stdDevCompute[i] - mean,2);
                differenceSquaredRunning += differenceSquared;
            }
            
            double stdDev = ((double)differenceSquaredRunning)/numTrials;
            
            switch(choice) {
                case 1: yAxis = mean;
                        break;

                case 2: yAxis = stdDev;
                        break;

                case 3: yAxis = time;
                        break;
            }

            System.out.println(p + "\t" + yAxis);

            System.err.println(mean + "\t" + stdDev + "\t" + time);
        }
    }
}

class Graph {
    private boolean[][] graph;
    private int numCCs;


    public Graph(int numNodes) {
        graph = new boolean[numNodes][];
        for(int i = 0; i < numNodes; i++) {
            graph[i] = new boolean[i];
        }
    }

    public int getNumCC() {
        return numCCs;
    }

    public Graph(boolean[][] graph) {
        this.graph = graph;
        depthFirstSearch();
    }

    public static Graph createRandomGraph(int numNodes, double probability) {
        Graph g = new Graph(numNodes);
        for(int x= 0; x < numNodes; x++) {
            for(int y = 0; y < g.graph[x].length; y++) {
                g.graph[x][y] = Math.random() < probability;
            }
        }
        g.depthFirstSearch();
        return g;
    }

    private void depthFirstSearch() {
        boolean[] visited = new boolean[graph.length];
        for(int i = graph.length - 1; i >= 0; i--) {
            if(!visited[i]) {
                numCCs++;
                visitEachAdjoining(i,visited);
            }
        }
    }

    private void visitEachAdjoining(int i, boolean[] visited) {
        visited[i] = true;
        for(int x = graph[i].length - 1; x >= 0 ; x-- ) {
            if(!visited[x]) {
                if(graph[i][x]) {
                    visitEachAdjoining(x, visited);
                }
            }
        }
        for(int x = i+1; x < graph.length; x++) {
            if(!visited[x]) {
                if(graph[x][i]) {
                    visitEachAdjoining(x, visited);
                }
            }
        }
    }

    public String toString() {
        String s = "";
        for(int x = 0; x < graph.length; x++) {
            for(int y = 0; y < graph[x].length; y++) {
                s += graph[x][y] ? 1 : 0;
            }
            s += "\n";
        }
        return s;
    }
}
