package Model;

import Model.Elements.*;


import java.io.*;
import java.util.*;

public class Simulation extends Observable implements Serializable, Observer{

	private List<List<Element>> cells;  //This will hold a 2D array of all cells in the simulation
    private List<Agent> agents;
	private Set<Element> activeCells;   //This holds all cells in the simulation which are on fire or near fire
                                            //as these are the only ones who need to be updated
	private List<Simulation> states;    //This holds a list of previous states of the simulation if undo is set to true
                                            //otherwise it will only hold the first state for reset
    private int steps_taken = 0;

    private int width;
    private int height;
    private int step_time;
    private int step_size;
    private int nr_agents=5;
    private boolean undo_redo;

    private ParameterManager parameter_manager;

	private boolean running;    //Boolean on whether the simulation it performing steps
    private boolean use_gui;

	private Random rand; //initializes RNG
    private long randomizer_seed = 0;

	public Simulation(boolean use_gui)
	{
        System.out.println("use_gui= " + use_gui );
	    this.use_gui = use_gui;
	    //Initialize these things
        Random seed_gen = new Random();
        randomizer_seed = seed_gen.nextLong();
        rand = new Random(randomizer_seed);
        states = new ArrayList<>();

        //Initialize the parameters to some default values and make them available for drawing
        create_parameters();

        parameter_manager = new ParameterManager(this);
        parameter_manager.addObserver(this);

        //This creates an area of trees of x by y, since we don't have the actual map generation yet
        tree_grid(width, height);
        //This gathers the first set of cells to be active
		findActiveCells();
        //This adds the initial state to the states list
		//states.add((Simulation) deepCopy(this));
		if(!use_gui){
		    start();
        }
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
            if(step_time >=0){
                stepForward();
            }else{
                stepBack();
            }
            try {
                Thread.sleep(Math.abs((long) step_time));
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
	        stop();

            rand = new Random(randomizer_seed);
            states = new ArrayList<>();
            tree_grid(width, height);
            findActiveCells();

            setChanged();
            notifyObservers(cells);
            notifyObservers(agents);

    }

    /**
     * Clears all the cells and active cells and draws a new map.
     * Currently this is the tree_grid since we don't have a map generation.
     */
    public void regenerate() {
        stop();
        states.clear();
        activeCells.clear();
        tree_grid(width, height);
        findActiveCells();
        states.add((Simulation) deepCopy(this));
    }

    /**
     * Revert the simulation by one time step if undo/redo is enabled.
     * If there are no steps to take back anymore, the simulation is paused.
     * Linked to both the Step back button, as well as running the simulation with a negative step time.
     */
    public void stepBack(){
	    if(undo_redo == true){
	        for(int i = 0; i< step_size; i++) {
	            if(steps_taken > 0){
	                reset();
	                for(int j= 0; j < steps_taken; j++){
	                    stepForward();
                        setChanged();
                        notifyObservers(cells);
                    }

                }else{
	                running = false;
                }
                /*
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
                */
            }
        }
    }

    /**
     * Perform one step forward (and record the previous state if undo/redo is enabled).
     * The step forward is performed by updateEnvironment(), and the new state is sent to the GUI with notifyObservers()
     */
    public void stepForward(){
        for(int i = 0; i< step_size; i++) {
            if (undo_redo == true) {
                System.out.println("Adding undo_copy");
                states.add((Simulation) deepCopy(this));
                steps_taken++;
            }
            updateEnvironment();
        }
        setChanged();
        notifyObservers(cells);
        notifyObservers(agents);
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
			String status = cell.update(cells, agents);
			if (status.equals("Dead"))
			{
				toRemove.add(cell);
			}
			if (status.equals("Ignited"))
			{
				toAdd.addAll(cell.getNeighbours(cells, agents));
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
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				Element cell = cells.get(x).get(y);
				if (cell.isBurning())
				{
					activeCells.addAll(cell.getNeighbours(cells, agents));
				}
			}
		}

		for (int i = 0; i<nr_agents; i++){
            activeCells.add(agents.get(i));
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
        agents = new ArrayList<>();
        cells = new ArrayList<>();
        for(int i = 0; i<x; i++){
            List<Element> col = new ArrayList<>();
            for(int j=0; j<y; j++){
                //Set a random tile on fire
                if(i== fire_x && j == fire_y) {
                    System.out.println("Fire at x= "+ i+ " y = " + j);
                    Element t = new Tree(i,j, parameter_manager);
                    t.setBurning();
                    col.add(t);
                } else {
                    if (i == 9 || i == 10) {
                        col.add(new Water(i, j, parameter_manager));
                    } else if (i == 12) {
                        col.add(new House(i, j, parameter_manager));
                    } else if (i == 14) {
                        col.add(new Road(i, j, parameter_manager));
                    } else if (j < 0.2*height) {
                        col.add(new Tree(i, j, parameter_manager));
                    } else {
                        col.add(new Grass(i, j, parameter_manager));
                    }
                }
            }
            cells.add(col);
        }

        // Instead of adding individual agents, all agents will be stored in a final ArrayList added to the tree-grid. This way the amount of agents can be modified easily.
        //This will create one agents which can will be dropped on a random location on the map.
        for (int i = 0; i<nr_agents; i++) {
            agents.add(new Agent(this));
        }

        //cells.add(agents);
        setChanged();
        notifyObservers(cells);
        //notifyObservers(agents);
    }

    /**
     * This sets all tunable parameters to a default value, and adds it to the list of TextFields tuneable at runtime
     * Due to HashMap restrictions it only works with Strings and Floats, so you should initialize a value with 3f.
     * If you want to access the value of a parameter do parameters.get("Parameter name").floatValue()
     */
    public void create_parameters() {
        width = 50;
        height = 50;
        if(use_gui) {
            step_time = 100;
        }else{
            step_time = 0;
        }
        step_size = 1;
        undo_redo = false;
    }


    /**
     * Return the parameters currently set, to be used by the parameter manager.
     * The values are defined in createParameters()
     * @return
     */
    public Map<String, Float> getParameters() {
        //TODO!
        //return parameters;
        Map<String, Float> return_map = new HashMap<>();
        return_map.put("Width", (float) width);
        return_map.put("Height", (float) height);
        return_map.put("Step Size", (float) step_size);
        return_map.put("Step Time", (float) step_time);
        return_map.put("Undo/Redo", undo_redo ? 1f : 0f);
        return return_map;
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

    /**
     * Simulation is observer to the parameterManager.
     * When the parameterManager changes something this update function is called,
     * with Object o a Map.Entry<String, Map.Entry<String, Float>>.
     * This way Object holds the recipient (here model, elsewhere i.e. Tree), the Parameter (i.e. Width) and the value
     * @param observable
     * @param o
     */
    @Override
    public void update(Observable observable, Object o) {
        if(o instanceof Map.Entry
            && ((Map.Entry) o).getKey() instanceof String
            && ((Map.Entry) o).getValue() instanceof Map.Entry
                && ((Map.Entry) o).getKey() == "Model"
                ){  //IF a Map.Entry<String, Map.Entry<String, Float>> and the first string is "Model"
            Float value = (Float) ((Map.Entry) ((Map.Entry) o).getValue()).getValue();
            switch( (String) ((Map.Entry) ((Map.Entry) o).getValue()).getKey() ){
                case "Width":
                    width = value.intValue();
                    break;
                case "Height":
                    height = value.intValue();
                    break;
                case "Step Time":
                    if(use_gui){
                        step_time = value.intValue();
                    }
                    break;
                case "Step Size":
                    step_size = value.intValue();
                    break;
                case "Undo/Redo":
                    undo_redo = value.intValue() == 1;
                    break;
            }
        }
    }

    /**
     * Needed to give controlPanel access to parameterManager
     * @return
     */
    public ParameterManager getParameter_manager(){
        return parameter_manager;
    }
    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }

    public int getRandX() {return rand.nextInt(width);}
    public int getRandY() {return rand.nextInt(height);}

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    public int getNr_agents() {
        return nr_agents;
    }

    public void setNr_agents(int nr_agents) {
        this.nr_agents = nr_agents;
    }
}
