package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: ONLY COMPATIBLE WITH SINGLE AGENT!!!
public class OrthogonalSubGoals implements Serializable {
    List<List<Element>> cells;
    int fireX, fireY;
    String algorithm;
    Map<String,Double> distMap;

    private static Map<Agent, String> agentGoals; //semi-solution for multi agent problem
    private String defaultKey = "WW"; //If no other goal can be selected resort to the default goal
    final int maxNrGoals = 8;

    private HashMap<String, Element> subGoals;
    private HashSet<String> goalsReached;


    //used for directions of subgoals
    final int dx[]={-1,-1,0,1,1,1,0,-1};
    final int dy[]={0,-1,-1,-1,0,1,1,1};

    //Might be usefull when linking the goal direction implementation of the features class to the implementation
    // in this class. It simply maps NESW coordinates to an index in the dx dy arrays
    private Map<String,Integer> compassMap = Stream.of(
            new AbstractMap.SimpleEntry<>("WW", 0),
            new AbstractMap.SimpleEntry<>("SW", 1),
            new AbstractMap.SimpleEntry<>("SS", 2),
            new AbstractMap.SimpleEntry<>("SE", 3),
            new AbstractMap.SimpleEntry<>("EE", 4),
            new AbstractMap.SimpleEntry<>("NE", 5),
            new AbstractMap.SimpleEntry<>("NN", 6),
            new AbstractMap.SimpleEntry<>("NW", 7))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    /**
     * The constructor still uses the dist[] to get the distance of the subgoal to the center of the fire. This however
     * means that the order in which the distances are stored in dist[] determines to which subGoal the distance
     * is assigned. The second constructor uses a HashMap, which gets rid of this problem.
     * @param fireX
     * @param fireY
     * @param dist
     * @param algorithm
     * @param cells
     */
    public OrthogonalSubGoals(int fireX, int fireY, double dist[], String algorithm, List<List<Element>> cells){

        this.fireX = fireX;
        this.fireY = fireY;
        this.algorithm = algorithm;
        this.cells = cells;

        agentGoals = new HashMap<>();
        goalsReached= new HashSet<>();

        distMap = new HashMap<>();
        for (String key:compassMap.keySet()){
            distMap.put(key, dist[compassMap.get(key)]);
        }

        subGoals = new HashMap<>();
        for (String key: distMap.keySet()){
            subGoals.put(key, getCorrespondingCell(key));
        }
    }

    /**
     * Using a hashMap instead of an int[] for storing the distances, allows for more robust storage of distance storage.
     * The distance of the subgoal to the center of the fire is not linked to the relative location of that subgoal to
     * the fire. By doing so, the order in which the subGoals are stored in the Map is no longer relevant.
     * @param fireX
     * @param fireY
     * @param distMap
     * @param algorithm
     * @param cells
     */
    public OrthogonalSubGoals(int fireX, int fireY, Map<String,Double> distMap, String algorithm, List<List<Element>> cells){
        this.fireX = fireX;
        this.fireY = fireY;
        this.algorithm = algorithm;
        this.cells = cells;

        agentGoals = new HashMap<>();
        goalsReached= new HashSet<>();

        this.distMap = distMap;

        subGoals = new HashMap<>();
        for (String key: distMap.keySet()){
            subGoals.put(key, getCorrespondingCell(key));
        }
    }

    /**
     * This function will assign the subGoal currently closest to the agent, if that goal has not been assigned to a
     * different agent. If it has already been assigned to another agent, look at the second closest goal. Continue
     * this until an unassigned goal has been reached.
     * @param a: The agent which needs a new subGoal.
     */
    public void selectClosestSubGoal(Agent a){ //TODO: Use RL for this step as well
        double minDist= Double.MAX_VALUE;
        String keyNearestGoal = null;
        System.out.println("Current goals in goalsReached:");
        for (String s:goalsReached){
            System.out.print(s + "+");
        }
        for (String key: distMap.keySet()){ //TODO: find out why the if statements below returns true for WW in current setting
            if (!(goalsReached.contains(getNextGoal(key))||agentDiggingTowardGoal(getNextGoal(key)))) { //Make sure only goals are assigned that have not been reached jet.
                SubGoal temp = new SubGoal(cells, subGoals.get(key), algorithm, a, false);
                if (minDist > temp.getMoveCost()) {
                    if (!agentGoals.containsValue(key)||(Collections.frequency(new ArrayList<String>(agentGoals.values()), key)==1 && agentDiggingTowardGoal(key))) { //TODO: make sure that at most one agent is moving towards this goal, and that this agent is not cutting.
                        minDist = temp.getMoveCost();
                        keyNearestGoal = key;
                    }
                }
                System.out.println("Tested key: " + key);
            }
        }
        if (keyNearestGoal==null){
            System.out.println("Going to default goal");
            keyNearestGoal = defaultKey;
        }
        if (!agentGoals.keySet().contains(a)){
            agentGoals.put(a, keyNearestGoal);
        } else {
            agentGoals.replace(a, keyNearestGoal);
        }
        updateAgentGoal(a, keyNearestGoal, subGoals.get(keyNearestGoal), false);
    }

    /**
     * Give an agent a new subGoal. If it reached its current subgoal, start cutting towards another subgoal. If not on
     * the current subGoal, move towards it.
     * @param agent the agent for which the goals need to be updated.
     */
    public void setNextGoal(Agent agent){
        if (!agentGoals.keySet().contains(agent)){
            agentGoals.put(agent, "WW");
            System.out.println("NO GOAL INITIALISED FOR AGENT #" + agent.getId());
        }
        Element goalCell = getCorrespondingCell(agentGoals.get(agent));
        if (!agentOnGoal(agent)){
            updateAgentGoal(agent, agentGoals.get(agent), goalCell, false);
        } else {
            if (agent.isCutting()) { // Only add the goal to the reached goal, it the goal was reached by a cutting agent.
                goalsReached.add(agentGoals.get(agent));
            }
            System.out.println("goalsReached.length: " + goalsReached.size());
            String nextGoal = getNextGoal(agentGoals.get(agent));
            if (goalsReached.contains(nextGoal)||agentDiggingTowardGoal(nextGoal)){ //If the goal has already been reached or another agent is cutting towards it, go to another goal.
                selectClosestSubGoal(agent);
            } else { //Otherwise, start cutting towards next goal.
                goalCell = getCorrespondingCell(nextGoal);
                updateAgentGoal(agent, nextGoal, goalCell, true);
            }
        }
    }

    public String getNextGoal(Agent a){
        return getNextGoal(agentGoals.get(a));
    }

    public String getNextGoal(String k){
        return getGoalKey((compassMap.get(k) + 1) % maxNrGoals);
    }


    public String getGoalKey(int i){
        return compassMap
                .entrySet()
                .stream()
                .filter(e -> e.getValue() == i)
                .findAny()
                .get()
                .getKey();
    }


    private boolean agentDiggingTowardGoal(String key){
        List<Map.Entry<Agent, String>>  entries=  agentGoals //Getting all agents which have the goal "key" assigned to it
                .entrySet()
                .stream()
                .filter(e -> e.getValue().equals(key))
                .collect(Collectors.toList());

        for (Map.Entry<Agent, String> e : entries){
            if (e.getKey().isCutting()){
                return true;
            }
        }
        return false;
    }

    /**
     * Update the goal of an agent and update the goal assigned to the agent in the agentGoals HashMap
     * @param agent: agent which goal needs to be updated.
     * @param subGoalKey: The key representing the subGoal.
     * @param goalCell: The actual subGoal.
     * @param cuttingToGoal: Whether or not the agent should cut towards the goal.
     */
    public void updateAgentGoal(Agent agent, String subGoalKey, Element goalCell, boolean cuttingToGoal){
        if (agentGoals.keySet().contains(agent)) {
            agentGoals.replace(agent, subGoalKey);
        } else {
            agentGoals.put(agent, subGoalKey);
        }
        System.out.println("Agent #" + agent.getId() + " now going to subGoal " + subGoalKey + " at coordinates " + subGoals.get(subGoalKey).toCoordinates());
        agent.setSubGoal(new SubGoal(cells, goalCell, algorithm, agent, cuttingToGoal));
        agent.setCutting(cuttingToGoal); //In oder to keep track if an agent reached its goal by cutting, the agent is either cutting or not
//        if (cuttingToGoal){ //If agent needs to start cutting, the current cell on which the agent is standing is a sub goal as the agent will only start cutting from a subGoal
//            cells.get(agent.getX()).get(agent.getX()).setReachedAsGoal(true);
//        }
    }


    /**
     * Simple check to see if the agent is standing on its current subGoal
     * @param agent The agents that needs to be checked.
     * @return
     */
    private boolean agentOnGoal(Agent agent){
        return (agent.getX() == xOfGoal(agentGoals.get(agent)) && agent.getY() == yOfGoal(agentGoals.get(agent)));
    }


    private int xOfGoal(String key){
        int goalNr = compassMap.get(key);
        if (dx[goalNr]*dy[goalNr]==0) {
            return Math.toIntExact(Math.round(distMap.get(key)))* dx[goalNr] + fireX;
        } else {
            return Math.toIntExact(Math.round(distMap.get(key)*dx[goalNr]/Math.sqrt(2)))+fireX;
        }
    }

    private int yOfGoal(String key){
        int goalNr = compassMap.get(key);
        if (dx[goalNr]*dy[goalNr]==0) {
            return Math.toIntExact(Math.round(distMap.get(key))) * dy[goalNr] + fireY;
        } else {
            return Math.toIntExact(Math.round(distMap.get(key)*dy[goalNr]/Math.sqrt(2)))+fireY;
        }
    }


    /**
     * Determine which cell belongs to the desired subGoal
     * @param key: the key which indicates the selected subgoal, i.e "WW", "SE" ect.
     * @return
     */
    public Element getCorrespondingCell(String key){

        int xDist = xOfGoal(key);
        int yDist = yOfGoal(key);
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

    /**
     * Update the distance of a subGoal in the distMap. If the to be updated goal is present in the keySet of distMap,
     * print an error and abort.
     * @param key: The key which represents the goal to be updated
     * @param dist: The new distance of the subGoal.
     */
    public void updateSubGoal(String key, double dist) {
        Double oldValue = distMap.replace(key, dist);
        if (oldValue == null){
            System.out.println("distMap value not updated successfully!! Selected key : " + key + ", complete keySet: " + Arrays.toString(distMap.keySet().toArray()));
            return;
        }
        if (subGoals.keySet().contains(key)){
            subGoals.replace(key, getCorrespondingCell(key));} else {
            subGoals.put(key, getCorrespondingCell(key));
        }
    }

    /**
     * function used to check whether a agent is able to reach the subGoal
     * @param key: The goal which needs to be checked
     * @param a: The agent that is supposed to reach the goal.
     * @return
     */
    public boolean checkSubGoal(String key, Agent a){
        SubGoal goal = new SubGoal(cells, subGoals.get(key), algorithm, a, false);
        return goal.pathExists();
    }

    /**
     * Debugging function. Removes the goal associated with the key
     * @param key: the key to goal to be colored.
     */
    public void paintGoal(String key){
        subGoals.get(key).colorGoal();
    }

    public Map<Agent, String> getAgentGoals() {
        return agentGoals;
    }

    public boolean isGoalReached(String key) {
        return goalsReached.contains(key);
    }

    public boolean isGoalOfAgent(String key) {
        return  agentGoals.values().contains(key);
    }

    public void removeGoalReached(String key){
        goalsReached.remove(key);
    }
}
