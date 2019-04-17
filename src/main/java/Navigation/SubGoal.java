package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;

public class SubGoal implements Serializable {
    Element goal;
    List<List<Element>> cells;
    String type;
    public Stack<Element> path;
    private Agent agent;

    public SubGoal(List<List<Element>> cells, Element goal, String type, Agent agent, boolean cutPath){
        this.goal = goal;
        this.cells = cells;
        this.type = type;
        this.agent = agent;
        if (agent.checkTile(goal.getX(), goal.getY())){
            determinePath(cutPath);
        } else {
            System.out.println("Invalid goal, pick another one");
        }
    }

    /**
     * If we with to add more path finding methods, we can do so by extending the switch statement.
     */
    private void determinePath(boolean cutPath){
        PathFinder pf;
        switch (type) {
            case "Dijkstra":
                pf = new DijkstraShortestPath(cells, agent, goal, cutPath);
                System.out.println("using Dijksta");
                break;
            default :
                pf = new BresenhamPath(cells, agent, goal, cutPath);
                System.out.println("using Bresenham");
        }
        pf.findPath();
        path = pf.getPath();
        //printPath(path);
    }

    /**
     * If it is possible to execute the movement necessary to move from the current location to the next location in
     * the path, execute that action. Otherwise do nothing
     * @return
     */

    public String getNextAction() {
        if (path == null || path.empty()){
            return "Do Nothing";
        }
        Element e = path.peek();
        String action = "default";
        int dx = e.getX()-agent.getX();
        int dy = e.getY()-agent.getY();
        if (dx==0){
            if (dy==1){
                action = "Go Up";
            } else if (dy==-1) {
                action = "Go Down";
            } else if (dy==0) {
                //TODO: This is an ad-hoc solution for making the agent dig a path instead of only walking over it.
                // Works for now, should be changed in a more robust function.
                action = "Dig";
            }
        } else if (dx==1){
            action = "Go Right";
        } else if (dx==-1){
            action = "Go Left";
        }
        if (agent.tryAction(action)){
            path.pop();
            return action;
        } else {
            return "Do Nothing";
        }
    }

    private boolean checkPath() {
        for (Element e : path) {
            if (e.isBurning()) {
                return false;
            }
        }
        return true;
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

    public Stack<Element> getPath() {
        return path;
    }
}
