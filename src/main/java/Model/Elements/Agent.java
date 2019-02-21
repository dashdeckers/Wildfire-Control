package Model.Elements;

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


    public Agent(int x, int y, Simulation simulation, ParameterManager parameterManager)
    {

        this.simulation = simulation;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
        this.x=x;
        this.y=y;

    }

    public Agent(Simulation simulation, ParameterManager parameterManager)
    {

        this.simulation = simulation;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
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
        this.burnIntensity = 2;
        this.ignitionThreshold = 1;
        this.fuel = 1;
        this.moveSpeed = 1;
        this.energyEachStep = 20;
    }

    /**
     * New tiles that cannot be transversed by an agent can be easily added to the function
     */

    private boolean checkTile(int x, int y) {
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

        takeActions();
        return super.timeStep();
    }

    private void takeActions() {
        energyLevel = energyEachStep;
        while(energyLevel>0 && fuel > 0) {
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
                    energyLevel=0;

            }
            //System.out.println("energy finish = " + energyLevel);
        }
    }

    private List<String> possibleActions() {
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
        return (int) ((double)energyEachStep/(double)e.getParameters().get("Move Speed"));
    }


    /**
     * All actions related to actual fire control
     */
    private void makeDirt() {
        Element cell = simulation.getAllCells().get(x).get(y);
        energyLevel-=cell.getParameters().get("Clear Cost");
        simulation.getAllCells().get(x).set(y, new Dirt(x, y, simulation.getParameter_manager()));

    }


    /**
     * All actions related to the movement of the agent
     */
    private void moveRight() {
        energyLevel-= determineMoveCost(simulation.getAllCells().get(x+1).get(y));
        x++;
    }

    private void moveLeft() {
        energyLevel-= determineMoveCost(simulation.getAllCells().get(x-1).get(y));
        x--;
    }

    private void moveDown() {
        energyLevel-= determineMoveCost(simulation.getAllCells().get(x).get(y-1));
        y--;
    }

    private void moveUp() {
        energyLevel-= determineMoveCost(simulation.getAllCells().get(x).get(y+1));
        y++;
    }



}
