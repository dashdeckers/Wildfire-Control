package Model.Elements;

import Model.Simulation;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Agent extends Element
{
    // The environment is fully observable for the agent, therefore in order to keep the information available to
    // the agent up to date, the agent needs access to the entire simulation.

    private Simulation simulation;
    private Element previousElement;


    private static final Color BLACK = new Color(0,0,0);
    public Agent(Simulation simulation)
    {

        this.simulation = simulation;
        this.parameters = simulation.getParameters();

        do {
            this.x = simulation.getRandX();
            this.y = simulation.getRandY();
            System.out.println("x= " + x + "\ny= " + y);
        } while (!checkTile(x,y));

        previousElement = simulation.getAllCells().get(this.x).get(this.y);

        initializeParameters();
    }

    public void initializeParameters()
    {
        this.r = 1;
        this.burnable = true;
        this.color = Color.YELLOW;
        this.burnIntensity = 2;
        this.ignitionThreshold = 10;
        this.fuel = 1;
        this.moveSpeed = 1;
    }

    /**
     * New tiles that cannot be transversed by an agent can be easily added to the function
     */

    public boolean checkTile(int x, int y) {
        if (inBounds(x,y)){
            Element element = simulation.getAllCells().get(x).get(y);
            switch(element.getType()) {
                case "Water":
                case "House":
                    return false;
                default:
                    return true;
            }
        } else {
            return false;
        }

    }

    @Override
    public String update(List<List<Element>> cells) {

        takeAction();
//        if (!burnable) {
//            return "Not Burnable";
//        }
//        boolean wasBurning = isBurning;
//        (super) timeStep();
//        if (wasBurning && !isBurning) {
//            updateFireActivity(cells, "remove");
//            return "Dead";
//        }
//        updateFireActivity(cells, "add");
//        if (!wasBurning) {
//            return "Ignited";
//        }
//        return "No Change";
        return  "Dead";
    }

    private void takeAction() {
        cutTree();
        if(checkTile(x , y+1)) {
            moveDown();
        }
        else if (checkTile(x+1, y)) {
            moveRight();
        }
    }

    private void cutTree() {
        Element cell = simulation.getAllCells().get(x).get(y);
        if((cell.getType().equals("Tree") || cell.getType().equals("Grass")) && (!cell.isBurnt())){
            simulation.getAllCells().get(x).set(y, new Road(x, y, simulation.getParameters()));
        }
    }

    private void moveRight() {
        x++;
    }

    private void moveDown() {
        y++;
    }

    @Override
    public String getType() {
        return "Agent";
    }



}
