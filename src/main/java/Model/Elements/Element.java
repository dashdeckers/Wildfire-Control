package Model.Elements;

import Model.ParameterManager;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.awt.Color;
import java.io.Serializable;
import java.util.*;

/*
	The abstract element class contains most of what is needed for each element,
	each subclass must implement initializeParameters() which defines the unique
	properties of each specific element.

	The important parameters are:

	Fuel: Large and dense biomass such as trees store a larger amount of energy,
	    so they can burn for longer. While burning, at each iteration the burn
	    intensity of the cell is subtracted from its fuel reserves

	Burn Intensity: Some material burns at a higher temperature, or intensity,
	    and so have a larger potential to ignite neighbouring cells. While
	    burning, at each iteration the burn intensity of the cell is added to
	    the fire activity of the neighbouring cells

	Fire Activity: A measure of how much heat the cell is experiencing from
	    nearby burning cells. If fire activity exceeds the ignition threshold,
	    the cell starts burning.

	Ignition Threshold: Some materials need a higher temperature to start burning
	    than others. This value should be set with the burn intensity, and fuel
	    parameters in mind because they are very co-dependent.
 */

public abstract class Element implements Serializable, Observer {

    // coordinates
    int x = 0;
    int y = 0;
    // radius of influence, if burning
    int r = 0;
    // color
    Color color = Color.WHITE;
    String type;
    // parameters passed from simulation
    ParameterManager parameterManager;
    // state properties
    boolean burnable = false;
    boolean isBurning = false;
    boolean isBurnt = false;
    // move speed: 0 is not traversable, 3 is easy to traverse
    int moveSpeed = 0;

    // parameters relevant for fire propagation
    int fireActivity = 0;
    int burnIntensity = 0;
    int ignitionThreshold = 10;
    int fuel = 0;

    int width;
    int height;

    public abstract void initializeParameters();

    /*
		Updates the cell and the fire activity of its neighbours, returns a
		simple status string to help keep track of active cells
	 */
    public String update(List<List<Element>> cells) {

        if (!burnable) {
            return "Not Burnable";
        }
        boolean wasBurning = isBurning;
        timeStep();
        if (wasBurning && !isBurning) {
            updateFireActivity(cells, "remove");
            return "Dead";
        }
        updateFireActivity(cells, "add");
        if (!wasBurning) {
            return "Ignited";
        }
        return "No Change";
    }

    /*
		At every time step, the fuel of the cell is reduced by its burnIntensity if the
		cell is burning, and it is burnt out of there is no more fuel left. If the cell
		is not burning, we check if the ignitionThreshold is reached and possibly ignite.
	 */
    private void timeStep() {
        if (isBurning) {
            fuel -= burnIntensity;
            if (fuel <= 0) {
                isBurning = false;
                isBurnt = true;
            }
        } else {
            if (fireActivity > ignitionThreshold) {
                isBurning = true;
            }
        }
    }

    /*
		Currently implemented as cumulative: Every time this is called, the cells within
		range (of circle provided by (x, y) and r) of this cell get the burnIntensity of
		this cell added to their fireActivity.
	 */
    private void updateFireActivity(List<List<Element>> cells, String command) {
        HashSet<Element> neighbours = getNeighbours(cells);
        for (Element cell : neighbours) {
            if (command.equals("add")) {
                cell.fireActivity += this.burnIntensity;
            }
            if (command.equals("remove")) {
                cell.fireActivity -= 3 * this.burnIntensity;
            }
        }
    }

    /*
		Returns a set of cells that fall within the range of this cell.
	 */
    public HashSet<Element> getNeighbours(List<List<Element>> cells) {
        HashSet<Element> neighbours = new HashSet<>();
        for (int xi = x - r; xi <= x + r; xi++) {
            for (int yi = y - r; yi <= y + r; yi++) {
                if (inBounds(xi, yi)) {
                    Element cell = cells.get(xi).get(yi);
                    if (cell.isWithinCircleOf(this)) {
                        neighbours.add(cell);
                    }
                }
            }
        }
        return neighbours;
    }

    /*
		Check if this cell is within range of another cell, defined by the circle given
		by (x, y) and r.
	 */
    private boolean isWithinCircleOf(Element cell) {
        return Math.pow(x - cell.x, 2) + Math.pow(y - cell.y, 2) <= Math.pow(r, 2);
    }

    /*
		Checks if the coordinates are within the boundaries of the map.
	 */
    private boolean inBounds(int x, int y) {
        int maxX = width;
        int maxY = height;
        if (x >= 0 && x < maxX
            && y >= 0 && y < maxY) {
                return true;
            }
        return false;
    }

    public void setBurning() {
        isBurning = true;
    }

    public boolean isBurning() {
        return isBurning;
    }

    public boolean isBurnt() {
        return isBurnt;
    }

    public boolean isBurnable() {
        return burnable;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getMoveSpeed()
    {
        return moveSpeed;
    }

    public Color getColor() {
        if (isBurning) {
            return Color.RED;
        } else if (isBurnt) {
            return Color.BLACK;
        } else {
            return color;
        }
    }

    /*
		Overriding hashCode() and equals() methods to effectively use HashSets
	 */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Element other = (Element) obj;
        if (x != other.x
         || y != other.y)
            return false;
        return true;
    }

    /**
     * Retrieves the default parameters set in the classes.
     * This is used by the ParameterManager to get default values
     * @return
     */
    public Map<String, Float> getParameters(){
        Map<String, Float> returnMap = new HashMap<>();
        returnMap.put("Radius", (float) r);
        returnMap.put("Move Speed", (float) moveSpeed);
        returnMap.put("Burn Intensity", (float) burnIntensity);
        returnMap.put("Ignition Threshold", (float) ignitionThreshold);
        //returnMap.put("Fuel", (float) fuel);

        return returnMap;
    }

    /**
     * Each element is observer to the parameterManager.
     * When the parameterManager changes something this update function is called,
     * with Object o a Map.Entry<String, Map.Entry<String, Float>>.
     * This way Object holds the recipient (here this.type, elsewhere Model), the Parameter (i.e. Radius) and the value
     * @param observable
     * @param o
     */
    @Override
    public void update(Observable observable, Object o) {
        System.out.println("Update parameter!");
        if(o instanceof Map.Entry
                && ((Map.Entry) o).getKey() instanceof String
                && ((Map.Entry) o).getValue() instanceof Map.Entry
                && ((Map.Entry) o).getKey() == this.type){
            Float value = (Float) ((Map.Entry) ((Map.Entry) o).getKey()).getValue();
            switch( (String) ((Map.Entry) ((Map.Entry) o).getKey()).getKey() ){
                case "Radius":
                    r = value.intValue();
                    break;
                case "Move Speed":
                    moveSpeed = value.intValue();
                    break;
                case "Burn Intensity":
                    burnIntensity = value.intValue();
                    break;
                case "Ignition Threshold":
                    ignitionThreshold = value.intValue();
                    break;
                case "Fuel":
                    //TODO! FUEL SHOULD NOT BE CALLED AT RUNTIME!!!
                    //fuel = value.intValue();
                    break;

            }
        }
    }

    /**
     * If the parameterManager holds values that our cell currently doesn't yet pullParameters ensures that we are up-to-date
     */
    public void pullParameters(){
        width = parameterManager.getWidth();
        height = parameterManager.getHeight();
        if(parameterManager.isChanged(this.type)) {
            System.out.println("Parameter pulled");
            Map<String, Float> typeMap = parameterManager.getParameterSet(this.type);
            r = typeMap.get("Radius").intValue();
            moveSpeed = typeMap.get("Move Speed").intValue();
            burnIntensity = typeMap.get("Burn Intensity").intValue();
            ignitionThreshold = typeMap.get("Ignition Threshold").intValue();
            //fuel = typeMap.get("Fuel").intValue();
        }
    }


}