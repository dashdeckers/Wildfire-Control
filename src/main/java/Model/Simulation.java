package Model;

import Model.Elements.Element;
import Model.Elements.Tree;
import Model.Elements.Grass;
import Model.Elements.Water;
import Model.Elements.House;
import Model.Elements.Road;


import java.io.*;
import java.util.*;

public class Simulation extends Observable implements Serializable{

	private List<List<Element>> cells;  //This will hold a 2D array of all cells in the simulation
	private Set<Element> activeCells;   //This holds all cells in the simulation which are on fire or near fire
                                            //as these are the only ones who need to be updated
	private List<Simulation> states;    //This holds a list of previous states of the simulation if undo is set to true
                                            //otherwise it will only hold the first state for reset
	private Map<String, Float> parameters;  //This holds the parameters drawn on the GUI (create_Parameters() for info)
	private Map<String, Float> staged_parameters; //This hold parameters which need to be imported at regeneration (width&height)
	private boolean running;    //Boolean on whether the simulation it performing steps
    private boolean use_gui;

	private Random rand; //initializes RNG

	public Simulation(boolean use_gui)
	{
	    this.use_gui = use_gui;
	    //Initialize these things
        parameters = new LinkedHashMap<>();
        staged_parameters = new HashMap<>();
        rand = new Random();
        states = new ArrayList<>();

        //Initialize the parameters to some default values and make them available for drawing
        create_parameters();

        //This creates an area of trees of x by y, since we don't have the actual map generation yet
        tree_grid(parameters.get("Width").intValue(), parameters.get("Height").intValue());

        //This gathers the first set of cells to be active
		findActiveCells();

        //This adds the initial state to the states list
		states.add((Simulation) deepCopy(this));
	}

	/*
		Debugging function to print the currently active cells
	 */
	private void printActiveCells(boolean showCells)
	{
		System.out.println("Number of active Cells: " + activeCells.size());
		if (!showCells)
		{
			return;
		}
		String output = "";
		for (Element cell : activeCells)
		{
			output += "(" + cell.getX() + " " + cell.getY() + ") ";
		}
		System.out.println(output);
	}


    /**
     * Start is linked to the start button. It moves one step forward every Step_time in ms.
     * A negative time will make it perform steps backwards, but only if undo/redo is enabled.
     * The loop will stop when running is set to false by calling stop() or pressing the stop button.
     */
    public void start() {
	    running = true;
        while(running){
            if(parameters.get("Step time") >=0){
                stepForward();
            }else{
                stepBack();
            }
            try {
                Thread.sleep(Math.abs((long) parameters.get("Step time").floatValue()));
            } catch (java.lang.InterruptedException e){
                System.out.println(e.getMessage());
            }

        }
    }

    /**
     * Pauses the simulation, linked to the stop button
     */
    public void stop(){
        running = false;
    }

    /**
     * Resets the simulation to the first state since the last regeneration. Linked to the reset button.
     */
    public void reset(){
	    if(states.size() > 0){
	        stop();
	        Simulation rewind = (Simulation) deepCopy(states.get(0));
	        this.cells = rewind.cells;
	        this.activeCells = rewind.activeCells;
            setChanged();
            notifyObservers(cells);
        }
    }

    /**
     * Clears all the cells and active cells and draws a new map.
     * Currently this is the tree_grid since we don't have a map generation.
     */
    public void regenerate() {
        stop();
        for (String s : staged_parameters.keySet()) {
            parameters.put(s, staged_parameters.get(s));
        }
        states.clear();
        activeCells.clear();
        tree_grid(parameters.get("Width").intValue(), parameters.get("Height").intValue());
        findActiveCells();
        states.add((Simulation) deepCopy(this));
    }

    /**
     * Revert the simulation by one time step if undo/redo is enabled.
     * If there are no steps to take back anymore, the simulation is paused.
     * Linked to both the Step back button, as well as running the simulation with a negative step time.
     */
    public void stepBack(){
	    if(parameters.get("Undo/redo").intValue() == 1){
            if (states.size() > 0) {
                Simulation rewind = states.get(states.size() - 1);
                states.remove(states.size() - 1);
                this.cells = rewind.cells;
                this.activeCells = rewind.activeCells;
                setChanged();
                notifyObservers(cells);
            } else {
                running = false;
            }
        }
    }

    /**
     * Perform one step forward (and record the previous state if undo/redo is enabled).
     * The step forward is performed by updateEnvironment(), and the new state is sent to the GUI with notifyObservers()
     */
    public void stepForward(){
	    if(parameters.get("Undo/redo").intValue() == 1) {
            states.add((Simulation) deepCopy(this));
        }
        updateEnvironment();
        setChanged();
        notifyObservers(cells);
    }

    /**
     * This returns the 2D matrix of all cells currently in the simulation.
     * @return
     */
    public List<List<Element>> getAllCells() {
        return cells;
    }

    /*
    	Updates all activeCells, removing burnt out cells and adding newly active cells (= cells
    	that are- or are nearby burning cells) along the way
     */
    public void updateEnvironment()
	{
		// remember elements to add to- or remove from set because we can't while iterating
		HashSet<Element> toRemove = new HashSet<>();
		HashSet<Element> toAdd = new HashSet<>();

		for (Element cell : activeCells)
		{
			String status = cell.update(cells);
			if (status.equals("Dead"))
			{
				toRemove.add(cell);
			}
			if (status.equals("Ignited"))
			{
				toAdd.addAll(cell.getNeighbours(cells));
			}
		}
		activeCells.addAll(toAdd);
		activeCells.removeAll(toRemove);
		//If the fire has stopped, stop the simulation
		if(activeCells.size() == 0){
		    running = false;
		}
	}

	/*
		Initializes activeCells by searching the entire map for burning cells
		and adding those and their neighbours
	 */
    private void findActiveCells()
	{
	    activeCells = new HashSet<>();
		for (int x = 0; x < parameters.get("Width").intValue(); x++)
		{
			for (int y = 0; y < parameters.get("Height").intValue(); y++)
			{
				Element cell = cells.get(x).get(y);
				if (cell.isBurning())
				{
					activeCells.addAll(cell.getNeighbours(cells));
				}
			}
		}
	}

    /**
     * Creates a grid of all tree cells with one random burning cell.
     * This is just for ensuring fire is spreading as it should and that the visualization is working.
     * @param x
     * @param y
     */
	private void tree_grid(int x, int y){
        int fire_x = rand.nextInt(x);
        int fire_y = rand.nextInt(y);
        cells = new ArrayList<List<Element>>();
        for(int i = 0; i<x; i++){
            List<Element> row = new ArrayList<Element>();
            for(int j=0; j<y; j++){
                //Set a random tile on fire
                if(i== fire_x && j == fire_y) {
                    Element t = new Tree(i,j, parameters);
                    t.setBurning();
                    row.add(t);
                } else {
                    if (i == 9 || i == 10) {
                        row.add(new Water(i, j, parameters));
                    } else if (i == 12) {
                        row.add(new House(i, j, parameters));
                    } else if (i == 14) {
                        row.add(new Road(i, j, parameters));
                    } else if (j%5 == 0) {
                        row.add(new Tree(i, j, parameters));
                    } else {
                        row.add(new Grass(i, j, parameters));
                    }
                }
            }
            cells.add(row);
        }
        setChanged();
        notifyObservers(cells);
    }

    /**
     * This sets all tunable parameters to a default value, and adds it to the list of TextFields tuneable at runtime
     * Due to HashMap restrictions it only works with Strings and Floats, so you should initialize a value with 3f.
     * If you want to access the value of a parameter do parameters.get("Parameter name").floatValue()
     */
    public void create_parameters() {
        parameters.put("Width", 50f); //Set the width of the simulation in cells
        parameters.put("Height", 50f); //Set the height of the simulation in cells
        if(use_gui){
            parameters.put("Step time", 250f);
        }else {
            parameters.put("Step time", 100f); //The time the simulation waits before performing the next step in ms
        }
        parameters.put("Step size", 1f); //When doing manual steps this says how many steps to perform per button press
        parameters.put("Undo/redo", 0f); //Set whether it is possible to undo/redo by values 0/1
        //Setting undo/redo to 1 will use a lot of memory

    }



    /**
     * Update a parameter with a String s and a float v.
     * If the parameter does not exist it will now be instantiated.
     * This is used by the text fields.
     * If you want to add a new parameter to the gui do that in create_parameters.
     *
     * Parameters added to staged_parameters will be loaded on the next regenerate, to prevent conflicts.
     * @param s
     * @param v
     */
    public void changeParameter(String s, float v){
        switch (s){
            case "Height": //Height and size will only be set when regenerate() is called.
            case "Width" :
                staged_parameters.put(s,v);
                break;
            default:
                parameters.put(s, v);
        }

    }

    /**
     * Return the parameters currently set
     * @return
     */
    public Map<String, Float> getParameters(){
        return parameters;
    }


    /**
     * This makes a full copy of any Serializable object, including it's children.
     * This is needed for being able to revert to previous states and circumventing Java's pass-by-reference
     * It's probably best to just leave this code as is unless you understand what is going on here.
     *
     */
    private static Object deepCopy(Object object) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
            outputStrm.writeObject(object);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            return objInputStream.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
