package Model;

import Learning.RLController;
import Model.Elements.Dirt;
import Model.Elements.Element;
import Model.ParameterManager;
import Model.Simulation;
import Navigation.DijkstraShortestPath;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class Agent implements Serializable{
    // The environment is fully observable for the agent, therefore in order to keep the information available to
    // the agent up to date, the agent needs access to the entire simulation.

    private Simulation simulation;
    private RLController controller;
    private ParameterManager parameterManager;

    private int x;
    private int y;
    private int id;
    protected String type;
    private boolean isAlive;
    private int energyLevel;
    private Color color;

    //Optimal path found by A* will be stored here
    private Stack<String> plan;

    /**
     * Create an agent at X,Y with a certain id.
     * @param x
     * @param y
     * @param simulation
     * @param parameterManager
     * @param id
     */
    public Agent(int x, int y, Simulation simulation, ParameterManager parameterManager, int id) {
        this.simulation = simulation;
        this.parameterManager = parameterManager;
        initializeParameters();
        this.id=id; //TODO! Do we need the ID or is that only for debugging?
        this.x=x;
        this.y=y;
    }

    private void initializeParameters(){
        this.isAlive=true;
        this.color=Color.YELLOW;
    }

    /**
     * Checks whether the agent can move over the tile at x,y.
     * If new elements which can't be traversed are made, they can be added here
     * @param x
     * @param y
     * @return
     */
    public boolean checkTile(int x, int y) {
        if (inBounds(x,y)){
            Element element = simulation.getAllCells().get(x).get(y);
            //TODO! Can't we use the Element.moveSpeed == 0 as defined around Element ~68
            switch(element.getType()) {
                case "Water":
                    return false;
                default:
                    return true;
            }
        } else {
            return false;
        }

    }

    boolean inBounds(int x, int y) {
        int maxX = parameterManager.getWidth();
        int maxY = parameterManager.getHeight();
        return x >= 0 && x < maxX
                && y >= 0 && y < maxY;
    }

    /**
     * Perform a timeStep as called by the Simulation
     * @return
     */
    public String timeStep() {
        /**
         * TODO: Properly define goal for each agent. If current code is uncommented, all agents will move towards x=0, y=0
         *
         *
         */
        // TODO: This is giving index out-of-bounds errors
        /*
        if (plan==null){
            DijkstraShortestPath sp = new DijkstraShortestPath(simulation.getAllCells(),this,simulation.getAllCells().get(49).get(49));
            sp.findPath();
            plan=sp.getDirections();
        }
        */
        //String returnString = super.timeStep();
        energyLevel=simulation.getEnergyAgents();
        takeActions();
        if (!isAlive){
            color=Color.MAGENTA;
            return "Dead";
        }
        return "Alive";
    }

    /**
     * Makes the agent perform an action, either by some controller, or simply at random
     */
    private void takeActions() {
        String currentAction;

        while(energyLevel>0 && isAlive) {
            //If an agent controller is assigned, have it make the decision
            if(controller != null) {
                controller.pickAction(this);
            }
//            }else {
//                if (plan.empty()) {
//
//                } else {
//                    currentAction = plan.pop();
//                }
                List<String> actions = possibleActions();
                Random r = new Random();
                currentAction = actions.get(r.nextInt(actions.size()));
                switch (currentAction) {
                    case "Cut Tree":
                    case "Cut Grass":
                        makeDirt();
                        break;
                    case "Go Down":
                        moveDown();
                        break;
                    case "Go Up":
                        moveUp();
                        break;
                    case "Go Right":
                        moveRight();
                        break;
                    case "Go Left":
                        moveLeft();
                        break;
                    default:
                        doNothing();
                }
                Element currentCell = simulation.getAllCells().get(x).get(y);
                if (currentCell.isBurning()){
                    isAlive=false;
                }
            }
            simulation.applyUpdates();
        }
    }

    /**
     * Returns a list of valid action which can be taken at this time
     * @return
     */
    public List<String> possibleActions() {
        List<String> actions = new ArrayList<>();
        Element currentCell = simulation.getAllCells().get(x).get(y);

        if (energyLevel >= currentCell.getParameters().get("Clear Cost") && currentCell.getType().equals("Tree")){
            actions.add("Cut Tree");
        }
        if (energyLevel >= currentCell.getParameters().get("Clear Cost") && currentCell.getType().equals("Grass")){
            actions.add("Cut Grass");
        }
        if (checkTile(x, y - 1) && (determineMoveCost(simulation.getAllCells().get(x).get(y-1)))<=energyLevel){
            actions.add("Go Down");
        }
        if (checkTile(x, y + 1) && (determineMoveCost(simulation.getAllCells().get(x).get(y+1)))<=energyLevel) {
            actions.add("Go Up");
        }
        if (checkTile(x + 1, y) && (determineMoveCost(simulation.getAllCells().get(x+1).get(y)))<=energyLevel) {
            actions.add("Go Right");
        }
        if (checkTile(x - 1, y) && (determineMoveCost(simulation.getAllCells().get(x-1).get(y)))<=energyLevel) {
            actions.add("Go Left");
        }

        actions.add("Do Nothing");
        return actions;
    }

    public int determineMoveCost(Element e){
        return (int) ((double)simulation.getEnergyAgents()/(double)e.getParameters().get("Move Speed"));
    }

    /**
     * Assign a controller to pick all actions for this agent
     * @param controller    An RLController implementation which picks an action when the agent calls
     *                      pickAction(Agent this)
     */
    public void setController(RLController controller){
        this.controller = controller;
    }

    /**
     * Check whether the tile should be made dirt by this agent. If it should not, call doNothing() and waste the fuel
     */
    public void makeDirt() {
        Element cell = simulation.getAllCells().get(x).get(y);
        if(energyLevel >= cell.getParameters().get("Clear Cost") && cell.getType().equals("Tree")
                ||energyLevel >= cell.getParameters().get("Clear Cost") && cell.getType().equals("Grass")
                ) {
            energyLevel -= cell.getParameters().get("Clear Cost");
            simulation.getAllCells().get(x).set(y, new Dirt(x, y, simulation.getParameter_manager()));
        }else{
            doNothing();
        }

    }

    /**
     * Check whether the agent can move right. If it can't, call doNothing(), waste the fuel and execute action on next timestep()
     */
    public void moveRight() {
        if(checkTile(x + 1, y)) {
            int actionCost = determineMoveCost(simulation.getAllCells().get(x + 1).get(y));
            if (actionCost <= energyLevel) {
                energyLevel -= actionCost;
                x++;
                return;
            }
        }
        doNothing();

    }

    /**
     * Check whether the agent can move left. If it can't, call doNothing(), waste the fuel and execute action on next timestep()
     */
    public void moveLeft() {
        if(checkTile(x - 1, y)) {
            int actionCost = determineMoveCost(simulation.getAllCells().get(x - 1).get(y));
            if (actionCost <= energyLevel) {
                energyLevel -= actionCost;
                x--;
                return;
            }
        }
        doNothing();
    }

    /**
     * Check whether the agent can move down. If it can't, call doNothing(), waste the fuel and execute action on next timestep()
     */
    public void moveDown() {
        if(checkTile(x, y - 1)){
            int actionCost = determineMoveCost(simulation.getAllCells().get(x).get(y - 1));
            if (actionCost<=energyLevel){
                energyLevel -= actionCost;
                y--;
                return;
            }
        }
        doNothing();
    }

    /**
     * Check whether the agent can move up. If it can't, call doNothing(), waste the fuel and execute action on next timestep()
     */
    public void moveUp() {
        if(checkTile(x, y + 1)){
            int actionCost = determineMoveCost(simulation.getAllCells().get(x).get(y + 1));
            if (actionCost<=energyLevel) {
                energyLevel -= actionCost;
                y++;
                return;
            }
        }
        doNothing();
    }

    /**
     * Set the fuel to 0, but increment the fitness with the remaining fuel
     */
    public void doNothing(){
        energyLevel=0;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Map<String, Float> getParameters(){
        Map<String, Float> returnMap = new HashMap<>();
        returnMap.put("Energy Level", (float) energyLevel);
        return returnMap;
    }

    public Color getColor() {
        return color;
    }
}
