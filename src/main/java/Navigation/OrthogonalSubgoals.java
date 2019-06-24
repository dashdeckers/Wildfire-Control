package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.io.Serializable;
import java.text.DecimalFormat;
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

    //ToDo: make automatic
    int nextGoal[][] = { {0,1,2,3,4,5,6,7}, {0,1,2,3,4,5,6,7}, {1,1,1,1,1,1,1,1} }; //  temporarily used as ad hoc solution.
    final int maxNrGoals = 8;
    int setGoal = 0;
    boolean testSubGoalSpreading = true;

    private List<Agent> agents;
    private double[][] distAgentToSubgoal;
    private Simulation model;
    private double[][] closestSubgoalToAgent;

    double[] dist;

    //used for saving x,y locations
    public double XYSub[] = new double[17];

    //used for directions of subgoals
    int dx[]={-1,-1,0,1,1,1,0,-1};
    int dy[]={0,-1,-1,-1,0,1,1,1};



    public OrthogonalSubgoals(int fireX, int fireY, double dist[], String algorithm, List<List<Element>> cells, Simulation model){
        this.fireX = fireX;
        this.fireY = fireY;
        this.dist = model.getSubGoals();
        this.algorithm = algorithm;
        this.cells = cells;
        this.model = model;
        this.agents = model.getAgents();




    }

    /**
     * give an agent a new goal. If it already is on a subgoal, start cutting towards another subgoal. If not on
     * the current goal, move towards it.
     * //@param agent the agent for which the goals need to be updated.
     */
    //TODO: make possible multiple agents
    public void setNextGoal(){//Agent agent){

        //System.out.println("###############in setNextGoal#################");

        distAgentToSubgoal = model.getDistAgentToSubgoal();
        closestSubgoalToAgent = model.getClosestSubgoalToAgent();


        if ( distAgentToSubgoal[model.getNr_agents()-1][7] != 0) {

//            DecimalFormat df = new DecimalFormat("###.##");
//            System.out.println("distAgentToSubGoal:");
//            for (int i = 0; i < model.getNr_agents(); i++){
//                for (int j = 0; j < 8; j++){
//                    System.out.print(df.format(distAgentToSubgoal[i][j]) + " ");
//                }
//                System.out.print("\n");
//            }
//            System.out.println("--------------------------");


            ////////////////////////////////// Assign  closes subgoal //////////////////////////////////
            //TODO: fix 1-8, 0-7 problem
            double distance = 2;
            int closestSubgoal = 0;
            for (int i = 0; i < model.getNr_agents(); i++)
            {
                for (int j = 0; j < 8; j++)
                {
                    // Always true first time since array is normalized
                    if (distAgentToSubgoal[i][j] < distance)
                    {
                        distance = distAgentToSubgoal[i][j];
                        closestSubgoal = j;
                    }
                }
                //System.out.println("i, clsub, distance = " + i + " " + closestSubgoal + " " + + distance);
                closestSubgoalToAgent[0][i] = closestSubgoal;
                distance = 2;
            }

            for (int i = 0 ; i < model.getNr_agents(); i ++){
                nextGoal[0][i] = (int)closestSubgoalToAgent[0][i];
            }

//            System.out.println("nextGoal = ");
//            System.out.println(Arrays.deepToString(nextGoal).replace("], ", "]\n"));
//            System.out.println("--------------------------");

            if (testSubGoalSpreading) {

                ////////////////////////////////// Sort subgoals ascending //////////////////////////////////
                bubbleSort(nextGoal, 0);

//                System.out.println("nextGoal = Bubblesorted 0 \n ----------");
//                System.out.println("nextGoal = ");
//                System.out.println(Arrays.deepToString(nextGoal).replace("], ", "]\n"));
//                System.out.println("--------------------------");

                ////////////////////////////////// Get rid of duplicates + fix sign direction doubles //////////////////////////////////
                for (int i = 0; i < model.getNr_agents(); i++) {
                    if (nextGoal[0][i] == nextGoal[0][(i + 1) % 8]) // if this one and next one are the same
                    {
                        nextGoal[2][i] = -1; // Directly change sign direction

                        int duplicate = nextGoal[0][i];
                        for (int j = i + 2; j < model.getNr_agents(); j++) {
                            if (nextGoal[0][j] == duplicate) // If more are the same
                            {
                                nextGoal[0][j] = nextGoal[0][j] + 1; // increase by one
                            }
                        }
                    }
                }

//                System.out.println("nextGoal without triples= ");
//                System.out.println(Arrays.deepToString(nextGoal).replace("], ", "]\n"));
//                System.out.println("--------------------------");


                //TODO: 7 &1 together
                ////////////////////////////////// Assign close agents to same subgoal //////////////////////////////////
                int i = 0;
                for (int j = 1; j <= 2; j++) // To ensure direct adjacent goals are first changed
                {
                    while (i < model.getNr_agents()) {
                        //          +1 since we have goal 0                & next goals are not the same , %8 so array is circular
                        if ((Math.abs((nextGoal[0][i] + 1) - (nextGoal[0][(i + 1) % 8] + 1)) == j) && (nextGoal[0][(i + 1) % 8] != nextGoal[0][(i + 2) % 8])
                                //Math abs since otherwise can be -1
                                && (nextGoal[0][(i) % 8] != nextGoal[0][Math.abs((i - 1)) % 8])) // & last goals are not the same
                        {
                            if (j == 1) {
                                nextGoal[0][(i + 1) % 8] = nextGoal[0][i];
                                nextGoal[2][i] = -1; // Change direction of movement so they don't follow the same path
                            } else { // Difference is 2, so take middle number
                                nextGoal[0][i] = nextGoal[0][i] + 1;
                                nextGoal[0][(i + 1) % 8] = nextGoal[0][(i + 1) % 8] - 1;
                                nextGoal[2][i] = -1; // Change direction of movement so they don't follow the same path
                            }

                            i++; // skip step since nextGoal[0][(i + 1)%7] == nextGoal[0][i];
                        }
                        i++;
                    }
                    i = 0;

                    ////////////////////////////////// Spread agents over circle //////////////////////////////////

//                    System.out.println("Before Spreading = ");
//                    System.out.println(Arrays.deepToString(nextGoal).replace("], ", "]\n"));
//                    System.out.println("--------------------------");
//
//                    int middle = model.getNr_agents()/2;
//
//                    int nrOfGaps = maxNrGoals - model.getNr_agents();
//                    int diffBetweenAgents = maxNrGoals / model.getNr_agents();
//                    int distancesDifference[] = new int[model.getNr_agents()-1];
//
//                    //Even or Odd
//                    if (model.getNr_agents()%2 == 0 || model.getNr_agents()%2 == 1){
//                        for(int k = 0; k< model.getNr_agents()-1; k++){
//                            distancesDifference[k] = Math.abs(nextGoal[0][k]  - nextGoal[0][k+1]);
//                        }
//                    }
//                    if (model.getNr_agents()%2 == 0){
//                        int middleDist = ( (model.getNr_agents()-1)/2 ) + 1;
//
//                        for (int l = 0; l < distancesDifference.length - middleDist; l++){
//                            // If difference is zero we have doubles so we need twice the space between points
//                            if (distancesDifference[middleDist + l] != 0 && distancesDifference[middleDist+1 + l] != 0){
//                                // Now the distance must be diffBetweenAgents
//                                // If distance is bigger:
//                                while (nextGoal[0][middleDist + 1 + l] - nextGoal[0][middleDist + l] > diffBetweenAgents){
//                                    nextGoal[0][middleDist + 1 + l] = nextGoal[0][middleDist + 1 + l] - 1;
//                                }
//                            }
//                            else if (distancesDifference[middleDist + l] == 0 && distancesDifference[middleDist+1 + l] != 0){
//                                // If this one is 0, distance to next must be 2* diffBetweenAgents
//                                while (nextGoal[0][middleDist + 1 + l] - nextGoal[0][middleDist + l] > 2*diffBetweenAgents){
//                                    nextGoal[0][middleDist + 1 + l] = nextGoal[0][middleDist + 1 + l] - 1;
//                                }
//                            } else if (distancesDifference[middleDist + l] != 0 && distancesDifference[middleDist+1 + l] == 0){
//                                while (nextGoal[0][middleDist + 1 + l] - nextGoal[0][middleDist + l] > 2*diffBetweenAgents){
//                                    nextGoal[0][middleDist + 1 + l] = nextGoal[0][middleDist + 1 + l] - 1;
//                                    nextGoal[0][middleDist + 2 + l] = nextGoal[0][middleDist + 2 + l] - 1;
//                                }
//                            } else if (distancesDifference[middleDist + l] == 0 && distancesDifference[middleDist+1 + l] == 0){
//                                while (nextGoal[0][middleDist + 1 + l] - nextGoal[0][middleDist + l] < 2*diffBetweenAgents){
//                                    nextGoal[0][middleDist + 1 + l] = nextGoal[0][middleDist + 1 + l] + 1;
//                                    nextGoal[0][middleDist + 2 + l] = nextGoal[0][middleDist + 2 + l] + 1;
//                                }
//                            }
//                        }


//                    System.out.println(Arrays.toString(distancesDifference));



                }


                ////////////////////////////////// Sort agents ascending //////////////////////////////////
                bubbleSort(nextGoal, 1);

//                System.out.println("nextGoal = Bubblesorted 1 \n ----------");
//                System.out.println(Arrays.deepToString(nextGoal).replace("], ", "]\n"));
//                System.out.println("-----------------------------------------------");
            }

//            //TODO: EXPORT GOAL + DIRECTION TO AGENT< LEAVE ARRAY AFTER INTIALIZATION
//            // Save values in agent
//            for (Agent agent : agents) {
//                agent.nextGoal = nextGoal[0][agent.getId()];
//                agent.direction =  nextGoal[2][agent.getId()];
//                Element goalCell = getCorrespondingCell(nextGoal[0][agent.getId()]);
//                agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, false));
//            }

            //TODO: fixt this, very sloppy
            setGoal = 1;
            distAgentToSubgoal[model.getNr_agents() - 1][7] = 0;
        }

        //System.out.println(Arrays.toString(XYSub));

        if (setGoal == 1) {
            //System.out.println("in setGoal == 1");
            for (Agent agent : agents) {
                //System.out.println("in forAgent, ID = " + agent.getId());
                if (agent.checkIfAlive() ) {
                    //System.out.println("in checkIfAlive");

                    //System.out.println("agent " + agent.getId() + " = " + agentOnGoal(agent));

                    if (!agentOnGoal(agent)) {
                        //nextGoal = (int) closestSubgoalToAgent[0][agent.getId()];
                        //System.out.println("number = " + (int) closestSubgoalToAgent[0][agent.getId()]);
                        //System.out.println("ID, nextGoal = " + agent.getId() + " " + nextGoal);

                        //System.out.println("in !onGoalAgent");
                        //System.out.println("coord. Agent = " + agent.getX() + ", " +agent.getY());

                        if (agent.nextGoal == 0) {
                            Element goalCell = getCorrespondingCell(nextGoal[0][agent.getId()]);

                            //System.out.println("Coord Goal = " + xOfGoal(nextGoal[0][agent.getId()]) + ", " + yOfGoal(nextGoal[0][agent.getId()]));

                            agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, false));

                            agent.nextGoal =1;
                        }

                    } else {
                        //System.out.println("in TRUEonGoalAgent");
                        //TODO: EXPORT GOAL + DIRECTION TO AGENT< LEAVE ARRAY AFTER INTIALIZATION
                        /////////// Added direction /////////
                        //System.out.println("Currentgoal = " + nextGoal[0][agent.getId()]);
                        int tempNextGoal = nextGoal[0][agent.getId()] + nextGoal[2][agent.getId()];
                        //System.out.println("tempNextGoal = " + tempNextGoal);
                        if (tempNextGoal < 0){ // Needed to be able to also subtract from 0, go to 7
                            tempNextGoal = 7;
                        }
                        nextGoal[0][agent.getId()] = ( tempNextGoal  )  % maxNrGoals;
                        //System.out.println("nextGoal = " + tempNextGoal);
                        Element goalCell = getCorrespondingCell(nextGoal[0][agent.getId()]);
                        agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, true));



                    }

//                    System.out.println(agentOnGoal(agent));
////                    if (!agentOnGoal(agent)){
////                        Element goalCell = getCorrespondingCell(agent.nextGoal);
////                        agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, false));
////                    } else {
//                    if(agentOnGoal(agent)){
//                        agent.nextGoal =(agent.nextGoal+agent.direction)%maxNrGoals;
//                        if(agent.nextGoal == -1) { agent.nextGoal = 7; }
//                        Element goalCell = getCorrespondingCell(agent.nextGoal);
//                        agent.setGoal(new SubGoal(cells, goalCell, algorithm, agent, true));
//                    }

                    //System.out.println("ID, nextGoal = " + agent.getId() + " " + agent.nextGoal);

                }
            }
            //System.out.println("-------------");
        }
        //System.out.println("-------------++++++++++++++");
    }


    public void bubbleSort(int[][] array, int row) {
        boolean sorted = false;
        int temp0;
        int temp1;
        int temp2;
        while(!sorted) {
            sorted = true;
            for (int i = 0; i < model.getNr_agents() - 1; i++) {
                if (array[row][i] > array[row][i+1]) {
                    temp0 = array[0][i];
                    temp1 = array[1][i];
                    temp2 = array[2][i];

                    array[0][i] = array[0][i+1];
                    array[1][i] = array[1][i+1];
                    array[2][i] = array[2][i+1];

                    array[0][i+1] = temp0;
                    array[1][i+1] = temp1;
                    array[2][i+1] = temp2;
                    sorted = false;
                }
            }
        }
    }


    private boolean agentOnGoal(Agent agent){
//        System.out.println("in agentOnGoal");
//        System.out.println("AgentID "+ agent.getId() + " = " + agent.getX() + ", " + agent.getY());
//        System.out.println("Goal = " + xOfGoal(nextGoal[0][agent.getId()]) + ", " + yOfGoal(nextGoal[0][agent.getId()]));
//        System.out.println((agent.getX() == xOfGoal(nextGoal[0][agent.getId()]) && agent.getY() == yOfGoal(nextGoal[0][agent.getId()])));
        //return (agent.getX() == xOfGoal(nextGoal[0][agent.getId()]) && agent.getY() == yOfGoal(nextGoal[0][agent.getId()]));
        //return (agent.getX() == xOfGoal( (int)closestSubgoalToAgent[0][agent.getId()]  ) && agent.getY() == yOfGoal( (int)closestSubgoalToAgent[0][agent.getId()] ));
        int goalNR = nextGoal[0][agent.getId()];
        int xGoal = (int)XYSub[ goalNR +1 ];
        int yGoal = (int)XYSub[ goalNR + 9];

//        System.out.println("in agentOnGoal");
//        System.out.println("Goal = " + xGoal + ", " + yGoal);
//        System.out.println("Agent = " + agent.getX() + ", " + agent.getY());
        return  (agent.getX() == xGoal) && (agent.getY() == yGoal) ;
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

        // Save current x,y location
        XYSub[(goalNr+1)] = xDist;
        XYSub[(goalNr+1)+8] = yDist;
//
//        System.out.println("goalNr = " + goalNr);
//        System.out.println("In get Corresponding cell");
//        System.out.println("X, Y = " + xDist + ", " + yDist);
        //System.out.println(Arrays.toString(XYSub));
        return cells.get(xDist).get(yDist);
    }

}
