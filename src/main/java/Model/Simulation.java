package Model;

import Model.Elements.*;

import java.io.*;
import java.util.*;

public class Simulation extends Observable implements Serializable, Observer{

	private List<List<Element>> cells;  //This will hold a 2D array of all cells in the simulation
    private Agent agent;
	private Set<Element> activeCells;   //This holds all cells in the simulation which are on fire or near fire
                                            //as these are the only ones who need to be updated
	private List<Simulation> states;    //This holds a list of previous states of the simulation if undo is set to true
                                            //otherwise it will only hold the first state for reset
    private int steps_taken = 0;

    private int width;
    private int height;
    private int step_time;
    private int step_size;
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
            notifyObservers(agent);

    }

    /**
     * Clears all the cells and active cells and draws a new map.
     * Currently this is the tree_grid since we don't have a map generation.
     */
    public void regenerate() {
        stop();
        states.clear();
        activeCells.clear();

        //tree_grid(parameter_manager.getWidth(), parameter_manager.getHeight());
        regenerateAuxiliary2(parameter_manager.getWidth(), parameter_manager.getHeight());

        findActiveCells();
        states.add((Simulation) deepCopy(this));
    }

    /**
     * Creates a randomly generated maps
     *
     * @param x
     * @param y
     */
    private void regenerateAuxiliary2(int x, int y){
        int area = x*y;

        /**
         * make two overarching variables:
         * 1) Rural : if high then amount of trees higher and amount of houses & roads lower
         * 2) Wetlands: If high then more rivers & lakes, if low then less rivers
         */
        int numberBushes = rand.nextInt((int) (0.2*area));
        int numberHouses = rand.nextInt((int) (0.05*area));
        cells = new ArrayList<List<Element>>();


        //
        // GRASS
        //
        // First fill with grass
        for(int i = 0; i<x; i++){
            List<Element> row = new ArrayList<Element>();

            for(int j=0; j<y; j++){
                row.add(new Grass(i, j, parameter_manager));
            }
            cells.add(row);
        }



        //
        // TREES
        //
        // Add Trees at random points
        for(int i = 0; i<x; i++){
            List<Element> row = cells.get(i);

            for(int j=0; j<y; j++){

                // chance = numberBushes/area that a tree is placed
                if( rand.nextInt(area) < numberBushes  ){

                    row.set(j, new Tree(i, j, parameter_manager));
                    //
                    // implement helper function to randomly place trees around
                    //
                }
            }
            cells.set(i, row);
        }


        //
        // HOUSES
        //
        // Add HOUSES at random points
        for(int i = 0; i<x; i++){
            List<Element> row = cells.get(i);

            for(int j=0; j<y; j++){

                // chance = numberBushes/area that a tree is placed
                if( rand.nextInt(area) < numberHouses  ){

                    row.set(j, new House(i, j, parameter_manager));
                    //
                    // Make sure houses are placed next to each other
                }
            }
            cells.set(i, row);
        }


        //
        // ROAD
        //
        // Add either a vertical or a horizontal road
        int chooseXY = rand.nextInt(2);


        // make vertical road (Starts at the top)
        if (chooseXY == 0){
            int randomX = rand.nextInt(x);

            List<Element> row = cells.get(randomX);
            for(int i = 0; i<y; i++){

                row.set(i, new Road(randomX, i, parameter_manager));
                cells.set(randomX, row);
            }


        // make horizontal road (Starts at the left)
        } else {

            int randomY = rand.nextInt(y);

            for(int i=0; i<x; i++){

                List<Element> row = cells.get(i);
                row.set(randomY, new Road(i, randomY, parameter_manager));
                cells.set(randomY, row);
            }

        }


        //
        // RIVER
        //
        // Add a meandering river, either starting at the left or at the top
        chooseXY = rand.nextInt(2);

        // make vertical river ( I believe this starts at the top)
        if (chooseXY == 0){

            // Ensure the south direction is implemented first
            int riverY = 0;
            int riverX = rand.nextInt(x);

            int west = 0;
            int east = 1;

            System.out.printf("----1\n");
            System.out.printf("riverX = %d\n", riverX);
            System.out.printf("riverY = %d\n", riverY);

            // Then let the river meander with a tendency to go south
            while (riverX >= 0 && riverX < x && riverY < y){

                System.out.printf("----2\n");
                System.out.printf("riverX = %d\n", riverX);
                System.out.printf("riverY = %d\n", riverY);

                List<Element> row = cells.get(riverX);
                row.set(riverY, new Water(riverX, riverY, parameter_manager));
                cells.set(riverY, row);

                int directionRiver = rand.nextInt(6);

                if (directionRiver == 0) { // West
                    riverX--;
                }
                if (directionRiver == 1 || directionRiver == 2 || directionRiver == 3 || directionRiver == 4) { // South, tendency to go south
                    riverY++;
                }
                if (directionRiver == 5) { // East
                    riverX++;

                }


            }

            System.out.printf("riverX = %d\n", riverX);
            System.out.printf("riverY = %d\n", riverY);
            System.out.printf("riverY ========================== %d\n", riverY);
        }/* else {

            // make horizontal river (Starts at the left)
            int riverX = 0;
            int riverY = rand.nextInt(y);
            row.add(new River(riverX, riverY, parameters));

            while(riverX >= 0 && riverX < x && riverY >= 0 && riverY < y){

                int directionRiver = rand.nextInt(3);
                if (directionRiver == 0) { // North
                    riverY--;
                    row.add(new River(riverX, riverY, parameters));
                }
                if (directionRiver == 1) { // West
                    riverX++;
                    row.add(new River(riverX, riverY, parameters));
                }
                if (directionRiver == 2) { // South
                    riverX++;
                    row.add(new River(riverX, riverY, parameters));
                }

            }
        }
        //
        // Also lakes?
        //
*/
/*
        //
        // FIRE
        //
        int fire_x = rand.nextInt(x);
        int fire_y = rand.nextInt(y);

        for(int i = 0; i<x; i++) {
            List<Element> row = cells.get(i);
            for (int j = 0; j < y; j++) {
                //Set a random tile on fire
                if (i == fire_x && j == fire_y) {
                    Element t = new Tree(i, j, parameters);
                    t.setBurning();
                    //row.add(t);
                    row.set(j, t);
                    cells.set(j, row);
                }
            }

        }
*/


        setChanged();
        notifyObservers(cells);


    }


    /**
     *
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
        notifyObservers(agent);
    }

    /**
     * This returns the 2D matrix of all cells currently in the simulation.
     * @return
     */
    public List<List<Element>> getAllCells() {
        return cells;
    }

	/**
	 *  Update the list of active cells. Apply the heat from the burning cell cell to all
	 *  of its neighbouring cells. If it ignites a neighbouring cell, add that cell to the
	 *  activeCells. If a burning cell runs out of fuel, remove it from the activeCells.
	 */
    public void updateEnvironment()
	{
		HashSet<Element> toRemove = new HashSet<>();
		HashSet<Element> toAdd = new HashSet<>();
		for (Element burningCell : activeCells)
		{
			String status = burningCell.timeStep();
			if (status.equals("Dead"))
			{
				toRemove.add(burningCell);
			}
			if (status.equals("No Change"))
			{
				HashSet<Element> neighbours = burningCell.getNeighbours(cells);
				for (Element neighbourCell : neighbours)
				{
					if (neighbourCell.isBurnable())
					{
						neighbourCell.getHeatFrom(burningCell);
						status = neighbourCell.timeStep();
						if (status.equals("Ignited"))
						{
							toAdd.add(neighbourCell);
						}
					}
				}
			}
		}
		activeCells.removeAll(toRemove);
		activeCells.addAll(toAdd);
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
					activeCells.add(cell);
				}
			}
		}
		activeCells.add(agent);
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
        //This will create one agent which can will be dropped on a random location on the map.
        agent = new Agent(this);
        setChanged();
        notifyObservers(cells);
        notifyObservers(agent);
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

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }
}
