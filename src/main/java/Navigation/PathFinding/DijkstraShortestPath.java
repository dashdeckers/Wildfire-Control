package Navigation.PathFinding;

import Model.Agent;
import Model.Elements.Element;

import java.io.Serializable;
import java.util.*;

public class DijkstraShortestPath extends PathFinder implements Serializable {

    public List<List<Element>> cells;
    public Agent agent;
    public Element goal;
    public Stack<Element> path;

    public boolean cutPath;

    /*
    For debugging: If set to true, the generated path will be painted gray
     */
    public boolean paintPath = false;


    // An efficient way to represent the directions "N", "W", "S", "E"
    int dx[]={0,-1,0,1};
    int dy[]={1,0,-1,0};

    /**
     * The cost[x][y] matrix will store the how much energy it will cost to go from the current location of the agent
     * to go to location x,y.
     */

    public int cost[][];

    public DijkstraShortestPath(List<List<Element>> cells, Agent agent, Element goal, boolean cutPath) {
        this.cells=cells;
        this.agent=agent;
        this.goal=goal;
        this.cutPath =cutPath;

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

        PriorityQueue<Node> st = new PriorityQueue<>(1,new NodeComparator());
        int agentX=agent.getX();
        int agentY=agent.getY();

        st.add(new Node(cells.get(agentX).get(agentY),0,null));

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
                        st.remove(new Node(cells.get(x).get(y),expectedCost(x,y),k));
                    }

                    cost[x][y]=cost[e.getX()][e.getY()]+getMoveCost(x,y);

                    st.add(new Node(cells.get(x).get(y),expectedCost(x,y),k));


                }
            }

            k = st.remove();
            e = k.getElement();

        } while (!e.equals(goal)&&!st.isEmpty());

        if (!e.equals(goal)){
            System.out.println("No path found :(");
        } else {
            makePath(k);
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

    public void makePath(Node node){
        Stack<Element> path = new Stack<>();
        Element cellAgent;

        /**
         * Check for the previous node, since the node on which the agent is currently standing should not be added.
         */
        while (node.getPreviousNode()!=null){

            if (paintPath){
                node.getElement().colorPath();
            }
            path.push(node.getElement());
            /**
             * If you want the agent to cut fires lines, set "cutPath" to true. The cell will be added to the path
             * twice. This will be interpreted by getNextAction() as a dig action.
             */
            if (cutPath && (node.getElement().getType().equals("Grass") || node.getElement().getType().equals("Tree"))) {
                path.push(node.getElement());
            }
            node = node.getPreviousNode();
        }

        //Final push to make sure the cell the agent is standing is also cut if needed.
        cellAgent = cells.get(agent.getX()).get(agent.getY());
        if (cutPath && (cellAgent.getType().equals("Grass") || cellAgent.getType().equals("Tree"))) {
            path.push(cellAgent);
        }
        //System.out.println("Returned to original location");
        this.path = path;
    }


    public Stack<Element> getPath(){ return this.path; }


    /**
     * This function will return the optimal path in terms of the actions the agents needs to take to reach its goal.
     */
    public Stack<String> getDirections2(){

        /*
            Since the path is approached from the goal towards the agent, the actions an agent can take are inverted.
            Where dx=0 and dy=-1 used to be going down, it is instead related to the agent needing to move upwards.
         */
        int newX=0,newY=0;
        int x=goal.getX();
        int y=goal.getY();
        Stack<String> directions =new Stack<>();

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
                            bestMove="Go Down";
                            break;
                        case 1:
                            bestMove="Go Right";
                            break;
                        case 2:
                            bestMove="Go Up";
                            break;
                        case 3:
                            bestMove="Go Left";
                            break;
                    }
                    newX=x+dx[i];
                    newY=y+dy[i];
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

}