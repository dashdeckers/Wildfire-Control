package Model.Elements;

import Model.ParameterManager;

import java.awt.Color;
import java.io.Serializable;
import java.util.*;

/*
	The abstract element class contains most of what is needed for each element,
	each subclass must implement initializeParameters() which defines the unique
	properties of each specific element.

	The important parameters are:

	Fuel: Large and dense biomass such as trees store a larger amount of energy,
	    so they can burn for longer. While burning, at each iteration the fuel
	    level is subtracted by 1.

	Burn Intensity: Some material burns at a higher temperature, or intensity,
	    and so have a larger potential to ignite neighbouring cells. While
	    burning, at each iteration the burn intensity of the cell is added to
	    the fire activity of the neighbouring cells, depending on the wind speed,
	    wind direction, and the distance to the cell.

	Fire Activity: A measure of how much heat the cell is experiencing from
	    nearby burning cells. If fire activity exceeds the ignition threshold,
	    the cell starts burning. This has a maximum value of 100.

	Ignition Threshold: Some materials need a higher temperature to start burning
	    than others. This value should be set with the burn intensity, and fuel
	    parameters in mind because they are very co-dependent.
 */

public abstract class Element implements Serializable, Observer {

    // coordinates
    int x = 0;
    int y = 0;
    // maximal radius of influence, if burning
    int r = 0;
    // color
    Color color = Color.WHITE;
    protected String type;
    // parameters passed from simulation
    ParameterManager parameterManager;
    // state properties
    boolean burnable = false;
    boolean isBurning = false;
    boolean isBurnt = false;
    // move speed: 0 is not traversable, 3 is easy to traverse
    int moveSpeed = 0;

    // parameters relevant for fire propagation
    double temperature = 0;
    int burnIntensity = 0;
    int ignitionThreshold = 10;
    int fuel = 0;

    int width;
    int height;

    public abstract void initializeParameters();


    /**
	 *	Updates the cell and the temperature of its neighbours, returns a
	 *	simple status string to help keep track of active cells
	 */
    public String update(List<List<Element>> cells)
    {
        // if not burnable, dont do anything
        if (!burnable) {
            return "Not Burnable";
        }
        // remember whether it was burning
        boolean wasBurning = isBurning;
        // update internal parameters
        timeStep();
        // if it is burnt out (=no more fuel), remove temperature
        if (isBurnt) {
            updateTemperature(cells, "remove");
            return "Dead";
        }
        if (isBurning)
        {
            // if it is burning and it was not burning, it just ignited
            updateTemperature(cells, "add");
            if (!wasBurning) {
                return "Ignited";
            }
        }
        // none of the above situations --> no change
        return "No Change";
    }

    /**
	 *	At every time step, the fuel of the cell is reduced by 1 if the cell is
	 *	burning. It is burnt out of there is no more fuel left. If the cell
	 *	is not burning, we check if the ignitionThreshold is reached and possibly
     *	ignite it.
	 */
    private void timeStep() {
        // if it is burning, we are using up fuel
        if (isBurning) {
            fuel -= 1;
            // if there is no more fuel, it is burnt out
            if (fuel <= 0) {
                isBurning = false;
                isBurnt = true;
            }
        } else {
            if (temperature > ignitionThreshold) {
                isBurning = true;
            }
        }
    }

    /*
		Currently implemented as cumulative: Every time this is called, the cells within
		range (of circle provided by (x, y) and r) of this cell get the burnIntensity of
		this cell added to their temperature.
	 */
    private void updateTemperature(List<List<Element>> cells, String command) {

        // burnIntensity is now used as maximal burnIntensity

        // should not be cumulative. one-shot application from neighbouring cells
        // instead of addition per update

        HashSet<Element> neighbours = getNeighbours(cells);
        for (Element cell : neighbours) {
            if (command.equals("add")) {
                cell.adjustTemperatureBy(calcTemperature(cell));
            }
            if (command.equals("remove")) {
                cell.adjustTemperatureBy(-1 * calcTemperature(cell));
            }
        }
    }

    private void adjustTemperatureBy(double amount)
    {
        double newAmount = temperature + amount;
        if (newAmount > 100)
        {
            newAmount = 100;
        }
        if (newAmount < 0)
        {
            newAmount = 0;
        }
        // range = [0, 100]
        temperature = newAmount;
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

    private double distanceTo(Element cell)
    {
        double d = Math.sqrt(Math.pow(x - cell.x, 2) + Math.pow(y - cell.y, 2));
        if (d > r)
        {
            d = r;
        }
        // range = [0, r]
        return d;
    }

    private double angleToWind(Element cell)
    {
        // vector between this cell and the given cell
        double cVecX = this.x - cell.x;
        double cVecY = this.y - cell.y;

        // wind vector
        double wVecX = 1;
        double wVecY = 1;

        // return angle between these two vectors (range = [-pi, pi])
        return Math.abs(Math.atan2(wVecX*cVecY - wVecY*cVecX, wVecX*cVecX + wVecY*cVecY));
    }

    private double calcTemperature(Element cell)
    {
        int windSpeed = 100;

        double distance = this.distanceTo(cell);
        if (distance == 0)
        {
            return burnIntensity;
        }
        double angle = this.angleToWind(cell);
        if (angle == 0)
        {
            return burnIntensity * (1 / distance);
        }
        // burnIntensity * ( 1 / ([0, r] + [-pi, pi]) )
        return burnIntensity * (1 / (distance + (angle / windSpeed)));
    }

    /*
		Checks if the coordinates are within the boundaries of the map.
	 */
    public boolean inBounds(int x, int y) {
        int maxX = width;
        int maxY = height;
        System.out.println();
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

    public boolean isBurnable() {
        return burnable;
    }

    public boolean isBurnt()
    {
        return isBurnt;
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

    public Color getColor()
    {
        if (isBurnt)
        {
            return Color.BLACK;
        }
        else if (isBurning)
        {
            return Color.RED;
        }
        else
        {
            if (temperature * 0.75 > ignitionThreshold)
            {
                return new Color(255, 72, 0);
            }
            if (temperature * 0.50 > ignitionThreshold)
            {
                return new Color(255,153,0);
            }
            if (temperature * 0.25 > ignitionThreshold)
            {
                return new Color(255, 255, 0);
            }
        }
        return color;
    }
/*
    public Color getColor()
    {
        if (isBurnt)
        {
            return Color.BLACK;
        }
        else if (isBurning)
        {
            return Color.RED;
        }
        else
        {
            if (temperature * 0.75 > ignitionThreshold)
            {
                double red = 255 * (temperature / 100);
                Color newCol = new Color((int)red, color.getBlue(), color.getGreen());
                return newCol;
            }
            else
            {
                return color;
            }
        }
    }
*/
/*
        if (isBurning) {
            return Color.RED;
        } else if (isBurnt) {
            return Color.BLACK;
        } else {
            if (temperature > 0)
            {
                double normalizedTemperature = temperature / 100f;
                return getRedGreenHue(normalizedTemperature);
            }
            return color;
        }
    }
*/
    private Color getRedGreenHue(double power)
    {
        double H = (1 - power) * 0.4; // Hue (note 0.4 = Green)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        return Color.getHSBColor((float)H, (float)S, (float)B);
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

    public String getType(){
        return this.type;
    }

}