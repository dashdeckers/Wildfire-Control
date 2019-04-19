package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.io.Serializable;
import java.util.List;

//TODO: ONLY COMPATIBLE WITH MULTIPLE AGENTS!!!
public class OrthogonalSubgoals implements Serializable {
    private List<SubGoal> subGoals;
    List<List<Element>> cells;
    int fireX, fireY;
    String algorithm;
    int nextGoal = 0; // not suitable when having multiple agents, temporarily used as ad hoc solution.
    final int maxNrGoals = 8;


    double[] dist;

    //used for directions of subgoals
    int dx[]={-1,-1,0,1,1,1,0,-1};
    int dy[]={0,-1,-1,-1,0,1,1,1};

    public OrthogonalSubgoals(int fireX, int fireY, double dist[], String algorithm, List<List<Element>> cells){
        this.fireX = fireX;
        this.fireY = fireY;
        this.dist = dist;
        this.algorithm = algorithm;
        this.cells = cells;
    }

    /**
     * give an agent a new goal. If it already is on a subgoal, start cutting towards another subgoal. If not on
     * the current goal, move towards it.
     * @param agent the agent for which the goals need to be updated.
     */
    public void setNextGoal(Agent agent){
        if (!agentOnGoal(agent)){
            Element goalCell = getCorrespondingCell(nextGoal);
            agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, false));
        } else {
            nextGoal=(nextGoal+1)%maxNrGoals;
            Element goalCell = getCorrespondingCell(nextGoal);
            agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, true));
        }
    }


    private boolean agentOnGoal(Agent agent){
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
        if(xDist < 0){
            xDist = 0;
        }
        if(xDist >= cells.size()){
            xDist = cells.size() -1;
        }
        if(yDist < 0){
            yDist = 0;
        }
        if(yDist >= cells.get(0).size()){
            yDist = cells.get(0).size() -1;
        }

        return cells.get(xDist).get(yDist);
    }

}
