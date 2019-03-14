package Model.Elements;

import Model.Agent;
import Model.ParameterManager;
import Model.Simulation;
import View.ControlPanel;
import View.MainFrame;

import java.awt.Color;
import java.io.Serializable;
import java.util.*;

/**
 The abstract element class contains most of what is needed for each element,
 each subclass must implement initializeParameters() which defines the unique
 properties of each specific element.

 The important parameters are:

 Wind: The direction and speed of the wind determines the heat spread, but
 this still needs some fine-tuning because currently a higher wind speed
 hinders the propogation of the fire

 Fuel: Large and dense biomass such as trees store a larger amount of energy,
 so they can burn for longer. While burning, at each iteration the fuel
 level is subtracted by 1.

 Burn Intensity: Some material burns at a higher temperature, or intensity,
 and so have a larger potential to ignite neighbouring cells. While
 burning, at each iteration a value based on the burnIntensity is added
 to neighbouring cells. This value is also based on the wind speed and
 direction, as well as distance to the burning cell.

 Fire Activity: A measure of how much heat the cell is experiencing from
 nearby burning cells. If fire activity exceeds the ignition threshold,
 the cell starts burning. This has a maximum value of 100.

 Ignition Threshold: Some materials need a higher temperature to start burning
 than others. This value should be set with the burn intensity, and fuel
 parameters in mind because they are very co-dependent. Actually, they all are.

 TL;DR:
 For radius 2, maximum 4 neighbours can give heat to one cell (maximum value = burn intensity)
 Ignition threshold is the temperature at which which it ignites
 Fuel is the num of iterations it can burn

 */

public abstract class Element implements Serializable, Observer {
	int x = 0;
	int y = 0;
	int r = 0;
	Color color = Color.WHITE;
	protected String type;

	private int width;
	private int height;

	boolean isBurnable = false;
	private boolean isBurning = false;

	ParameterManager parameterManager;

	// parameters relevant for fire propagation
	private double temperature = 0;
	int burnIntensity = 0;
	int ignitionThreshold = 10;
	int fuel = 0;
	int starting_fuel = 0;

	//parameters relevant for agent actions
	//int energyEachStep = 5;
	int clearCost = 0;
	// move speed: 0 is not traversable, 3 is easy to traverse
	int moveSpeed = 0;

	// wind parameters
	private double windSpeed;
	private double wVecX;
	private double wVecY;

	/**
	 *  Every element needs to initialize
	 */
	public abstract void initializeParameters();

	/**
	 *	If the element is burning, reduce fuel levels. If fuel levels are empty, return "Dead"
	 *  otherwise return "No Change".
     *  If the element is burnable, but does not burn right now, reduce temperature X every step.
	 *  If it is not burning, check if the heat levels are high enough. If they are higher than
	 *  ignition threshold, return "Ignited", otherwise return "No Change"
	 */
	public String timeStep() {
		if (isBurning) {
			fuel -= 1;
			if (fuel <= 0) {
				isBurning = false;
				return "Dead";
			}
		}
		return "No Change";
	}

	/**
	 *  Apply heat from burning cell, taking into account the windspeed,
	 *  wind direction, and distance to the burning cell.
	 */
	public String getHeatFrom(Element burningCell) {
		double distance = distanceTo(burningCell);
		double angle = angleToWind(burningCell);

		temperature += burnIntensity * Math.pow(windSpeed * angle + distance, -1);
		//System.out.println("Temperature increase by: BurnIntensity * " + Math.pow(windSpeed * angle + distance, -1));

		if (temperature > 100) {temperature = 100;}

		if (temperature > ignitionThreshold && !isBurning) {
			isBurning = true;
			return "Ignited";
		}
		return "No Change";
	}

	/**
	 *  Returns the pythagorean distance to the given cell
	 */
	public double distanceTo(Element cell)
	{
		return Math.sqrt(Math.pow(x - cell.x, 2) + Math.pow(y - cell.y, 2));
	}

	public int manhattanDistanceTo(Element cell) {
		return Math.abs(this.getX() - cell.getX() + Math.abs(this.getY() - cell.getY()));
	}
	/**
	 *  Returns the angle between the vector that is given between the two cells
	 *  and the vector that is given via the wind direction
	 */
	private double angleToWind(Element cell) {
		// vector between this cell and the given cell
		double cVecX = this.x - cell.x;
		double cVecY = this.y - cell.y;

		// return angle between that vector and the wind direction (range = [0, pi])
		return Math.abs(Math.atan2(wVecX*cVecY - wVecY*cVecX, wVecX*cVecX + wVecY*cVecY));
	}

	/**
	 *  Returns a set of neighbouring cells. The radius determines the amount of neighbours
	 *  by being the maximal amount of steps you can take from the origin to land on a cell
	 *  that still counts as being a neighbour. Only burnable neighbours are considered.
	 */
	public HashSet<Element> getNeighbours(List<List<Element>> cells, List<Agent> agents) {
		HashSet<Element> neighbours = new HashSet<>();
		int originX = getX();
		int originY = getY();
		for (int x = 0; x <= r; x++) {
			for (int y = 0; y+x <= r; y++) {
				if (x == 0 && y == 0) {
					continue;
				}
				if (inBounds(originX + x, originY + y)) {
				    Element cell = cells.get(originX + x).get(originY + y);
				    if (cell.isBurnable) {
                        neighbours.add(cell);
                    }
				}
				if (inBounds(originX + x, originY - y)) {
				    Element cell = cells.get(originX + x).get(originY - y);
				    if (cell.isBurnable) {
                        neighbours.add(cell);
                    }
				}
                if (inBounds(originX - x, originY + y)) {
                    Element cell = cells.get(originX - x).get(originY + y);
                    if (cell.isBurnable) {
                        neighbours.add(cell);
                    }
                }
                if (inBounds(originX - x, originY - y)) {
                    Element cell = cells.get(originX - x).get(originY - y);
                    if (cell.isBurnable) {
                        neighbours.add(cell);
                    }
                }
			}
		}
		return neighbours;
	}

	/**
	 * Checks if the coordinates are within the boundaries of the map.
	 */
	boolean inBounds(int x, int y) {
		int maxX = width;
		int maxY = height;
		return x >= 0 && x < maxX
				&& y >= 0 && y < maxY;
	}

	public void setBurning() {isBurning = true;}

	public boolean isBurning() {return isBurning;}

	public boolean isBurnable() {return isBurnable;}

	public int getX() {return x;}

	public int getY() {return y;}

	/**
	 * Returns the color based on the state. Black if burnt, Red if
	 * burning, otherwise 3 shades of orange based on temperature.
	 * Agents turn magenta when they die, for visibility.
	 */
	public Color getColor() {
		if (fuel <= 0 && isBurnable()) {
			return Color.BLACK;
		} else if (isBurning) {
			return new Color(200, 0, 0);
		} else {
			if (temperature > ignitionThreshold * 0.75) {
				return new Color(255, 100, 0);
			} else if (temperature > ignitionThreshold * 0.50)	{
				return new Color(255, 150, 0);
			} else if (temperature > ignitionThreshold * 0.25) {
				return new Color(255,200,0);
			}
		}
		return color;
	}

	/**
	 *	Overriding hashCode() and equals() methods to effectively use HashSets
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
		if (x != other.x || y != other.y)
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
		returnMap.put("Starting Fuel", (float) starting_fuel);

		//returnMap.put("Energy Level", (float) energyEachStep);
		returnMap.put("Clear Cost", (float) clearCost);

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
		if(o instanceof Map.Entry
				&& ((Map.Entry) o).getKey() instanceof String
				&& ((Map.Entry) o).getValue() instanceof Map.Entry
				&& ((Map.Entry) o).getKey() == this.type){
			Float value = (Float) ((Map.Entry) ((Map.Entry) o).getValue()).getValue();
			switch( (String) ((Map.Entry) ((Map.Entry) o).getValue()).getKey() ){
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
				case "Starting Fuel":
					//TODO! FUEL SHOULD NOT BE CALLED AT RUNTIME!!!
					starting_fuel = value.intValue();
					break;
				default:
					System.out.println("PARAMETER CHANGED BUT NOT DEFINED IN Element.update(...)");
			}
		}
	}

	/**
	 * If the parameterManager holds values that our cell currently doesn't yet pullParameters ensures that we are up-to-date
	 */
	void pullParameters(){
		width = parameterManager.getWidth();
		height = parameterManager.getHeight();
		wVecX = parameterManager.getParameterSet("Model").get("Wind x");
		wVecY = parameterManager.getParameterSet("Model").get("Wind y");
		windSpeed = parameterManager.getParameterSet("Model").get("Wind Speed");

		parameterManager.addObserver(this);
		if(parameterManager.isChanged(this.type)) {
			Map<String, Float> typeMap = parameterManager.getParameterSet(this.type);
			r = typeMap.get("Radius").intValue();
			moveSpeed = typeMap.get("Move Speed").intValue();
			burnIntensity = typeMap.get("Burn Intensity").intValue();
			ignitionThreshold = typeMap.get("Ignition Threshold").intValue();
			//energyEachStep = typeMap.get("Energy Level").intValue();
			clearCost = typeMap.get("Clear Cost").intValue();
			starting_fuel = typeMap.get("Starting Fuel").intValue();
			fuel = starting_fuel;
		}
	}

	public String getType() {
		return type;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public int getFuel(){
		return fuel;
	}

	// Debugging function for printing coordinates
	public String toCoordinates() {
		return "("+this.x+", "+this.y+")";
	}

	// Debugging function for painting the path of an agent.
	public void colorPath(){
		color = Color.LIGHT_GRAY;
	}

}