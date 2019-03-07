package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.util.List;

public class DijkstraShortestPath {

    public List<List<Element>> cells;
    public List<Agent> agents;
    public

    DijkstraShortestPath(List<List<Element>> cells, List<Agent> agents) {
        this.cells=cells;
        this.agents=agents;
    }


}
