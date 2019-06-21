package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import Learning.OffsetFeatures;
import Model.Simulation;

//TODO: ONLY COMPATIBLE WITH MULTIPLE AGENTS!!!
public class OrthogonalSubgoals implements Serializable {
    private List<SubGoal> subGoals;
    List<List<Element>> cells;
    int fireX, fireY;
    String algorithm;
    int nextGoal = 0; // not suitable when having multiple agents, temporarily used as ad hoc solution.
    final int maxNrGoals = 8;

    private List<Agent> agents;
    private double[][] distAgentToSubgoal;
    private Simulation model;
    private double[][] closestSubgoalToAgent;

    double[] dist;

    //used for saving x,y locations
    public double XYSub[] = new double[17]; // 17 places

    //used for directions of subgoals
    int dx[]={-1,-1,0,1,1,1,0,-1};
    int dy[]={0,-1,-1,-1,0,1,1,1};



    public OrthogonalSubgoals(int fireX, int fireY, double dist[], String algorithm, List<List<Element>> cells, Simulation model){
        this.fireX = fireX;
        this.fireY = fireY;
        this.dist = dist;
        this.algorithm = algorithm;
        this.cells = cells;
        this.model = model;
        this.agents = model.getAgents();


    }

    /**
     * give an agent a new goal. If it already is on a subgoal, start cutting towards another subgoal. If not on
     * the current goal, move towards it.
     * @param agent the agent for which the goals need to be updated.
     */
    //TODO: make possible multiple agents
    public void setNextGoal(){//Agent agent){


//
//
//        distAgentToSubgoal = model.getDistAgentToSubgoal();
//
//
//
//        // Assign agents to closest subgoal
//        // ToDo: Does not work like this, gets called all the time
//        //closestSubgoalToAgent = model.getClosestSubgoalToAgent();
//        closestSubgoalToAgent = model.getClosestSubgoalToAgent();
//
//        System.out.println(Arrays.deepToString(distAgentToSubgoal).replace("], ", "]\n"));
//        System.out.println("--------------------------");
//        System.out.println(Arrays.deepToString(closestSubgoalToAgent).replace("], ", "]\n"));
//        System.out.println("--------------------------");
//
//
//
//        /*  Array:
//                       A1  A2
//           Subgoals:  [ x   y ]
//           Direction: [ -1 -1 ]
//        */
//
//        for (int i = 0; i < model.getNr_agents(); i++) {
//            for (int j = 0; j < 8 ; j++)
//                // Always true first time since array is initialized at -1 and distances are normalized
//                if (closestSubgoalToAgent[0][i] < distAgentToSubgoal[i][j] || closestSubgoalToAgent[0][i] == -1) {
//                    closestSubgoalToAgent[0][i] = j;
//                }
//        }
//        System.out.println(Arrays.deepToString(closestSubgoalToAgent).replace("], ", "]\n"));
//        System.out.println("--------------------------");
//        // Check the distance between agents
//
//        //2 agents:
//
//        // Assign agents to new subgoals
//        if ( Math.abs(closestSubgoalToAgent[0][0] - closestSubgoalToAgent[0][1]) > 2 ){
//            closestSubgoalToAgent[0][1] = (closestSubgoalToAgent[0][0]+4)%8;
//        } else {
//            closestSubgoalToAgent[0][1] = closestSubgoalToAgent[0][0];
//            // Assign directions of agent[]
//            closestSubgoalToAgent[1][1] = 1;
//        }
//        System.out.println(Arrays.deepToString(closestSubgoalToAgent).replace("], ", "]\n"));
//        System.out.println("--------------------------");
//
//        //System.out.println(Arrays.deepToString(distAgentToSubgoal).replace("], ", "]\n"));
//        //System.out.println("--------------------------");
//
//        for (Agent agent : agents) {
//
//            System.out.println("AgentID = " + agent.getId());
//
//            if (!agentOnGoal(agent)) {
//                //Element goalCell = getCorrespondingCell(nextGoal);
//                Element goalCell = getCorrespondingCell( (int)closestSubgoalToAgent[0][agent.getId()] );
//                agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, false));
//            } else {
//                //nextGoal = (nextGoal + 1) % maxNrGoals;
//                // Add or subtract direction
//                closestSubgoalToAgent[0][agent.getId()] = (closestSubgoalToAgent[0][agent.getId()] + closestSubgoalToAgent[1][agent.getId()]) % maxNrGoals;
//
//                System.out.println("closestSubGoalToAgent = " + (int)closestSubgoalToAgent[0][agent.getId()]);
//
//                Element goalCell = getCorrespondingCell( (int)closestSubgoalToAgent[0][agent.getId()] );
//                agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, true));
//            }
//        }

//        System.out.println(Arrays.deepToString(distAgentToSubgoal).replace("], ", "]\n"));
//        System.out.println("--------------------------");
//
//        System.out.println(Arrays.deepToString(closestSubgoalToAgent).replace("], ", "]\n"));
//        System.out.println("--------------------------");

        distAgentToSubgoal = model.getDistAgentToSubgoal();
        closestSubgoalToAgent = model.getClosestSubgoalToAgent();

        for (int i = 0; i < model.getNr_agents(); i++) {
            for (int j = 0; j < 8 ; j++)
                // Always true first time since array is initialized at -1 and distances are normalized
                if (closestSubgoalToAgent[0][i] < distAgentToSubgoal[i][j] || closestSubgoalToAgent[0][i] == -1) {
                    closestSubgoalToAgent[0][i] = j;
                }
        }

        //Arrays.sort(myArr, (a, b) -> Double.compare(a[0], b[0]));

//        System.out.println(Arrays.deepToString(distAgentToSubgoal).replace("], ", "]\n"));
//        System.out.println("--------------------------");
//
//        System.out.println(Arrays.deepToString(closestSubgoalToAgent).replace("], ", "]\n"));
//        System.out.println("--------------------------");

        for (Agent agent : agents) {


            if (!agentOnGoal(agent)){
                Element goalCell = getCorrespondingCell(nextGoal);
                agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, false));

                System.out.println("ID, nextGoal = " + agent.getId() + " " + nextGoal);
            } else {
                nextGoal=(nextGoal+1)%maxNrGoals;
                Element goalCell = getCorrespondingCell(nextGoal);
                agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, true));

                System.out.println("ID, nextGoal = " + agent.getId() + " " + nextGoal);
            }
        }
    }


    private boolean agentOnGoal(Agent agent){
        return (agent.getX() == xOfGoal(nextGoal) && agent.getY() == yOfGoal(nextGoal));
        //return (agent.getX() == xOfGoal( (int)closestSubgoalToAgent[0][agent.getId()]  ) && agent.getY() == yOfGoal( (int)closestSubgoalToAgent[0][agent.getId()] ));
    }

    //Roel: public
    public int xOfGoal(int goalNr){
        if (dx[goalNr]*dy[goalNr]==0) {
            return (int) dist[goalNr] * dx[goalNr] + fireX;
        } else {
            return Math.toIntExact(Math.round(dist[goalNr]*dx[goalNr]/Math.sqrt(2)))+fireX;
        }
    }

    //Roel:Public
    public int yOfGoal(int goalNr){
        if (dx[goalNr]*dy[goalNr]==0) {
            return (int) dist[goalNr] * dy[goalNr] + fireY;
        } else {
            return Math.toIntExact(Math.round(dist[goalNr]*dy[goalNr]/Math.sqrt(2)))+fireY;
        }
    }



    private Element getCorrespondingCell(int goalNr){

        int xDist = xOfGoal(goalNr);
        int yDist = yOfGoal(goalNr);

        // Save current x,y location
        XYSub[(goalNr+1)] = xDist;
        XYSub[(goalNr+1)+8] = yDist;

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
