package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.util.*;

public class DijkstraShortestPath {

    public List<List<Element>> cells;
    public Agent agent;
    public Element goal;

    public DijkstraShortestPath(List<List<Element>> cells, Agent agent, Element goal) {
        this.cells=cells;
        this.agent=agent;
        this.goal=goal;
    }



    public void findPath(){

        //initialisation of distance matrix
        int dist[][] = new int[cells.size()][cells.get(0).size()];
        for (int i = 0; i<dist.length; i++) {
            for (int j = 0; j<dist[0].length; j++) {
                dist[i][j]=Integer.MAX_VALUE;
            }
        }
//
//        System.out.println("size x: " + cells.size() + " size y: " + cells.get(0).size());
//        System.out.println("dist.size x: " + dist.length + " dist.size y: "+ dist[0].length);
//

        PriorityQueue<Node> st = new PriorityQueue<>(1,new NodeComparator());
        int dx[]={1,0,-1,0};
        int dy[]={0,1,0,-1};

        int agentX=agent.getX();
        int agentY=agent.getY();

        int goalX=goal.getX();
        int goalY=goal.getY();

        st.add(new Node(cells.get(agentX).get(agentY),0));

        dist[agentX][agentY]=0;

        while (st.size()>0){
            Node k = st.remove();
            Element e = k.getElement();

            for (int i = 0; i<4; i++){

                int x = e.getX() + dx[i];
                int y = e.getY() + dy[i];

                if (!agent.checkTile(x, y)){
                    continue;
                }

                if (dist[x][y]>dist[e.getX()][e.getY()]+getMoveCost(x,y)&&dist[e.getX()][e.getY()]<=dist[goalX][goalY]){
                    if (dist[x][y]!=Integer.MAX_VALUE){
                        st.remove(new Node(cells.get(x).get(y),dist[x][y]));
                    }

                    dist[x][y]=dist[e.getX()][e.getY()]+getMoveCost(x,y);
                    st.add(new Node(cells.get(x).get(y),dist[x][y]));


                }
            }

        }

        printMatrix(dist);
        Stack<Element> path=findShortestPath(dist);
        printPath(path);
        System.out.println(path.pop().toCoordinates());


    }

    private int getMoveCost(int x, int y){
        return agent.determineMoveCost(cells.get(x).get(y));
    }

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

    public Stack<Element> findShortestPath(int dist[][]){
        int dx[]={1,0,-1,0};
        int dy[]={0,1,0,-1};
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

                if (dist[newX][newY]<costBestMove){
                    bestMove=cells.get(newX).get(newY);
                    costBestMove = dist[newX][newY];
                }
            }

            shortestPath.push(bestMove);
            x=bestMove.getX();
            y=bestMove.getY();

        }

        return shortestPath;

    }

    public void printPath(Stack<Element> path) {
        System.out.println("shortest path found from goal "+ goal.toCoordinates() +":");
        for (Element e:path){
            System.out.println("-> (" + e.getX() + ", " + e.getY() + ")");
        }
        System.out.println("Agent at: (" + agent.getX() + ", " + agent.getY() + ")");
    }


}