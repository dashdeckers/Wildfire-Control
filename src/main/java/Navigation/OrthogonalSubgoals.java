package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.io.Serializable;
import java.util.List;

//TODO: ONLY COMPATIBLE WITH A SINGLE AGENT!!!
public class OrthogonalSubgoals implements Serializable {
    private List<SubGoal> subGoals;
    List<List<Element>> cells;
    int fireX, fireY;
    String algorithm;
    int nextGoal = 0; // not useful when having multiple agents.
    final int maxNrGoals = 8; // Increase to 8 once diagonals are implemented.


    double[] dist;

    //used for directions
    int dx[]={-1,-1,0,1,1,1,0,-1};
    int dy[]={0,-1,-1,-1,0,1,1,1};

    public OrthogonalSubgoals(int fireX, int fireY, double dist[], String algorithm, List<List<Element>> cells){
        this.fireX = fireX;
        this.fireY = fireY;
        this.dist = dist;
        this.algorithm = algorithm;
        this.cells = cells;
    }

    public void setNextGoal(Agent agent){
        if (!agentOnGoal(agent)){
            Element goalCell = getCorrespondingCell(nextGoal);
            System.out.println(goalCell.toCoordinates());
            agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, false));
        } else {
            nextGoal=(nextGoal+1)%maxNrGoals;
            Element goalCell = getCorrespondingCell(nextGoal);
            System.out.println(goalCell.toCoordinates());
            agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, true));
        }
    }


    private boolean agentOnGoal(Agent agent){
        System.out.println("Goes to X: " + xOfGoal(nextGoal) + " Y: " + yOfGoal(nextGoal));
        return (agent.getX() == xOfGoal(nextGoal) && agent.getY() == yOfGoal(nextGoal));
    }


    private int xOfGoal(int goalNr){
        if (dx[goalNr]*dy[goalNr]==0) {
            return (int) dist[goalNr] * dx[goalNr] + fireX;
        } else {
            return Math.toIntExact(Math.round(dist[goalNr]*dx[goalNr]/Math.sqrt(2)))+fireX;
        }
    }

    private int yOfGoal(int goalNr){
        if (dx[goalNr]*dy[goalNr]==0) {
            return (int) dist[goalNr] * dy[goalNr] + fireY;
        } else {
            return Math.toIntExact(Math.round(dist[goalNr]*dy[goalNr]/Math.sqrt(2)))+fireY;
        }
    }


    private Element getCorrespondingCell(int goalNr){

        int xDist = xOfGoal(goalNr);
        int yDist = yOfGoal(goalNr);

        return cells.get(xDist).get(yDist);
    }

}
