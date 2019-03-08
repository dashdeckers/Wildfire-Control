package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.util.*;

public class DijkstraShortestPath {

    public List<List<Element>> cells;
    public Agent agent;
    public Element goal;
    int dx[]={1,0,-1,0};
    int dy[]={0,1,0,-1};

    /**
     * The cost[x][y] matrix will store the how much energy it will cost to go from the current location of the agent
     * to go to location x,y.
     */

    public int cost[][];

    public DijkstraShortestPath(List<List<Element>> cells, Agent agent, Element goal) {
        this.cells=cells;
        this.agent=agent;
        this.goal=goal;

        //initialisation of cost matrix
        cost = new int[cells.size()][cells.get(0).size()];
        for (int i = 0; i<cost.length; i++) {
            for (int j = 0; j<cost[0].length; j++) {
                cost[i][j]=Integer.MAX_VALUE;
            }
        }
    }



    /**
     * findPath() will primarily fill in the cost values for cost[][]. Ideally it keeps track of the optimal actions while
     * filling in the entries of cost[][], but I have not found a way to efficiently do this.
     */
    public void findPath(){


//
//        System.out.println("size x: " + cells.size() + " size y: " + cells.get(0).size());
//        System.out.println("dist.size x: " + dist.length + " dist.size y: "+ dist[0].length);
//

        PriorityQueue<Node> st = new PriorityQueue<>(1,new NodeComparator());
        int agentX=agent.getX();
        int agentY=agent.getY();

        int goalX=goal.getX();
        int goalY=goal.getY();

        st.add(new Node(cells.get(agentX).get(agentY),0));

        cost[agentX][agentY]=0;

        Node k = st.remove();
        Element e = k.getElement();

        //Continue until goal is reached, or if there are no states nodes to visit.
        do {

            for (int i = 0; i<4; i++){

                int x = e.getX() + dx[i];
                int y = e.getY() + dy[i];

                if (!agent.checkTile(x, y)){
                    continue;
                }

                /*
                Check is the current action will provide an improvement to the cost matrix
                 */
                if (cost[x][y]>cost[e.getX()][e.getY()]+getMoveCost(x,y)){

                    /*
                    If the cell had been visited before, remove the node corresponding to the cell from the queue
                     */
                    if (cost[x][y]!=Integer.MAX_VALUE){
                        st.remove(new Node(cells.get(x).get(y),expectedCost(x,y)));
                    }

                    cost[x][y]=cost[e.getX()][e.getY()]+getMoveCost(x,y);

                    st.add(new Node(cells.get(x).get(y),expectedCost(x,y)));


                }
            }

            k = st.remove();
            e = k.getElement();

        } while (!e.equals(goal)&&!st.isEmpty());

        if (e.equals(goal)){
            //printMatrix(cost);
            //Stack<Element> path=findShortestPath();
            //printPath(path);
            //printDirections(getDirections());
            //System.out.println(path.pop().toCoordinates());
        } else {
            System.out.println("No path found :(");
        }



    }

    /**
     * Not absolutely necessary, but makes the overall script more readable
     * @param x
     * @param y
     * @return
     */
    private int getMoveCost(int x, int y){
        return agent.determineMoveCost(cells.get(x).get(y));
    }

    /**
     * debugging function the check what values are stored in the matrix
     * @param m
     */
    public void printMatrix(int m[][]){
        System.out.println("The cost matrix looks as follows:");
        for (int i=0; i<m[0].length; i++){
            System.out.print("| ");
            for (int j=0; j<m.length; j++){
                System.out.print(m[j][m[0].length-i-1]+" ");
            }
            System.out.println("|");
        }
    }

    /**
     * The difference between the A* algorithm and Dijkstra's Algorithm is the way the expected cost is determined.
     * In Dijkstra's algorithm, to only parameters used to calculate the expected cost is the already determined cost
     * stored in cost[][].
     */
    private int expectedCost(int x, int y){
        return cost[x][y]+A_StarHeuristic(x,y);
    }


    /**
     * Current implementation of A* is simply the minimum amount of steps needed to get to the goal from the current cell
     * @param x
     * @param y
     * @return
     */
    private int A_StarHeuristic(int x, int y){
        return Math.abs(goal.getX()-x)+Math.abs(goal.getY()-y);

    }


    /**
     * This function will return the optimal path in terms of the actions the agents needs to take to reach its goal.
     */
    public Stack<String> getDirections(){

        /*
            Since the path is approached from the goal towards the agent, the actions an agent can take are inverted.
            Where dx=0 and dy=-1 used to be going down, it is instead related to the agent needing to move upwards.
         */
        int newX=0,newY=0;
        int x=goal.getX();
        int y=goal.getY();
        Stack<String> directions =new Stack<>();
        //shortestPath.push(goal);

        do {

            int costBestMove = Integer.MAX_VALUE;
            String bestMove=null;
            for (int i =0; i<4; i++){
                if (!agent.checkTile(x+dx[i], y+dy[i])){
                    continue;
                }

                if (cost[x+dx[i]][y+dy[i]]<costBestMove){
                    switch (i) {
                        case 0:
                            bestMove="Go Left";
                            break;
                        case 1:
                            bestMove="Go Down";
                            break;
                        case 2:
                            bestMove="Go Right";
                            break;
                        case 3:
                            bestMove="Go Up";
                            break;
                    }
                    newX=x+dx[i];
                    newY=y+dy[i];
                    //bestMove=cells.get(newX).get(newY);
                    costBestMove = cost[newX][newY];
                }
            }

            directions.push(bestMove);
            x=newX;
            y=newY;

        } while (!(x==agent.getX()&&y==agent.getY()));

        return  directions;
    }


    /**
     * This function will return the optimal path in terms of cells that will be visited while taking the optimal path
     */
    public Stack<Element> findShortestPath(){
        int newX=0,newY=0;
        int x=goal.getX();
        int y=goal.getY();
        Stack<Element> shortestPath=new Stack<>();
        shortestPath.push(goal);
        while (!(x==agent.getX()&&y==agent.getY())){

            int costBestMove = Integer.MAX_VALUE;
            Element bestMove=null;
            for (int i =0; i<4; i++){
                newX=x+dx[i];
                newY=y+dy[i];
                if (!agent.checkTile(newX, newY)){
                    continue;
                }

                if (cost[newX][newY]<costBestMove){
                    bestMove=cells.get(newX).get(newY);
                    costBestMove = cost[newX][newY];
                }
            }

            shortestPath.push(bestMove);
            x=bestMove.getX();
            y=bestMove.getY();

        }

        return shortestPath;

    }


    /**
     * debugging function for checking the optimal path
     */
    public void printPath(Stack<Element> path) {
        System.out.println("shortest path found from goal "+ goal.toCoordinates() +":");
        for (Element e:path){
            System.out.println("-> (" + e.getX() + ", " + e.getY() + ")");
        }
        System.out.println("Agent at: (" + agent.getX() + ", " + agent.getY() + ")");
    }

    public void printDirections(Stack<String> dir) {
        for (String s:dir){
            System.out.println("Next action for agent " + agent.getId()+ ": " + s);
        }
    }


}