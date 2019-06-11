package Navigation.PathFinding;

import Model.Agent;
import Model.Elements.Element;

import java.util.List;
import java.util.Stack;

public abstract class PathFinder {
    public Stack<Element> path;
    public Element goal;
    public Agent agent;
    public List<List<Element>> cells;
    public double finalMoveCost;
    public boolean cutPath;

    public PathFinder(List<List<Element>> cells, Agent agent, Element goal, boolean cutPath) {
        this.cells = cells;
        this.agent = agent;
        this.goal = cells.get(goal.getX()).get(goal.getY());
        this.cutPath = cutPath;
    }

    abstract public Stack<Element> getPath();
    abstract public void findPath();
    public double getFinalMoveCost(){
        if (!path.empty()) {
            return finalMoveCost;

        } else {
            System.out.println("The path you tried to access does not exists!");
            return Double.MAX_VALUE;
        }
    }
}
