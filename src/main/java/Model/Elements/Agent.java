package Model.Elements;

import Learning.RLController;
import Model.ParameterManager;
import Model.Simulation;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Agent extends Element
{
    // The environment is fully observable for the agent, therefore in order to keep the information available to
    // the agent up to date, the agent needs access to the entire simulation.

    private Simulation simulation;
    private int energyLevel;
    private static final Color BLACK = new Color(0,0,0);
    private RLController controller;
    private boolean isAlive;


    public Agent(int x, int y, Simulation simulation, ParameterManager parameterManager, int id)
    {

        this.simulation = simulation;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
        this.id=id;
        this.x=x;
        this.y=y;

    }

    public Agent(Simulation simulation, ParameterManager parameterManager, int id)
    {

        this.simulation = simulation;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
        this.id = id;
        do {
            this.x = simulation.getRandX();
            this.y = simulation.getRandY();
        } while (!checkTile(x,y));
    }


    public void initializeParameters()
    {
        this.type = "Agent";
        this.r = 1;
        this.isBurnable = true;
        this.color = Color.YELLOW;
        this.burnIntensity = 1;
        this.ignitionThreshold = 1;
        this.fuel = 1;
        this.moveSpeed = 1;
        simulation.setAgentsLeft(simulation.getAgentsLeft()+1);
        this.isAlive=true;
        //this.energyEachStep = 20;
    }

    /**
     * New tiles that cannot be transversed by an agent can be easily added to the function
     */

    public boolean checkTile(int x, int y) {
        if (inBounds(x,y)){
            Element element = simulation.getAllCells().get(x).get(y);
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

    @Override
    public String timeStep() {

        String returnString = super.timeStep();
        if (returnString.equals("Dead")&&isAlive){
            System.out.println("Agent " + getId() + " died.");
            simulation.setAgentsLeft(simulation.getAgentsLeft()-1);
            isAlive=false;
        } else if (isAlive){
            takeActions();
        }
        return returnString;
    }

    private void takeActions() {
        energyLevel = simulation.getEnergyAgents();
        while(energyLevel>0 && fuel > 0) {
            //If an agent controller is assigned, have it make the decision
            if(controller != null){
                controller.pickAction(this);
            }
            List<String> actions = possibleActions();
            //System.out.println("action list = " + actions.toString());
            Random r = new Random();
            String currentAction = actions.get(r.nextInt(actions.size()));
            //System.out.println("Decided to do: " + currentAction + ", having energy level: " + energyLevel + " and temperature: " + temperature);
            switch (currentAction){
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
            //System.out.println("energy finish = " + energyLevel);
        }
    }

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

    private int determineMoveCost(Element e){
        return (int) ((double)simulation.getEnergyAgents()/(double)e.getParameters().get("Move Speed"));
    }












    /**
     * Assign a controller to pick all actions for this agent
     * @param controller
     */
    public void setController(RLController controller){
        this.controller = controller;
    }

    /**
     * All actions related to actual fire control
     */
    public void makeDirt() {
        Element cell = simulation.getAllCells().get(x).get(y);
        if(energyLevel >= cell.getParameters().get("Clear Cost") && cell.getType().equals("Tree")
                ||energyLevel >= cell.getParameters().get("Clear Cost") && cell.getType().equals("Grass")
                ) {
            energyLevel -= cell.getParameters().get("Clear Cost");
            simulation.setFitness(simulation.getFitness() - Math.round(cell.getParameters().get("Clear Cost")));
            simulation.getAllCells().get(x).set(y, new Dirt(x, y, simulation.getParameter_manager()));
        }

    }

    /**
     * All actions related to the movement of the agent
     */

    public void moveRight() {
        Element currentCell = simulation.getAllCells().get(x).get(y);
        if (checkTile(x + 1, y) && (determineMoveCost(simulation.getAllCells().get(x+1).get(y)))<=energyLevel) {
            int actionCost = determineMoveCost(simulation.getAllCells().get(x + 1).get(y));
            energyLevel -= actionCost;
            x++;
        }
    }

    public void moveLeft() {
        Element currentCell = simulation.getAllCells().get(x).get(y);
        if (checkTile(x - 1, y) && (determineMoveCost(simulation.getAllCells().get(x-1).get(y)))<=energyLevel) {
            int actionCost = determineMoveCost(simulation.getAllCells().get(x - 1).get(y));
            energyLevel -= actionCost;
            x--;
        }
    }

    public void moveDown() {
        Element currentCell = simulation.getAllCells().get(x).get(y);
        if (checkTile(x, y - 1) && (determineMoveCost(simulation.getAllCells().get(x).get(y-1)))<=energyLevel){
            int actionCost = determineMoveCost(simulation.getAllCells().get(x).get(y - 1));
            energyLevel -= actionCost;
            y--;
        }
    }

    public void moveUp() {
        Element currentCell = simulation.getAllCells().get(x).get(y);
        if (checkTile(x, y + 1) && (determineMoveCost(simulation.getAllCells().get(x).get(y+1)))<=energyLevel) {
            int actionCost = determineMoveCost(simulation.getAllCells().get(x).get(y + 1));
            energyLevel -= actionCost;
            y++;
        }
    }

    public void doNothing(){
        simulation.setFitness(simulation.getFitness()+energyLevel);
        energyLevel=0;
    }

    //TODO!! Add requirements to actions!


}
