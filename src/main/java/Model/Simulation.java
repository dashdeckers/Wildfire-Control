package Model;

import Learning.RLController;
import Model.Elements.*;
import Navigation.DijkstraShortestPath;
//import com.sun.xml.internal.bind.v2.TODO;

import java.io.*;
import java.util.*;

public class Simulation extends Observable implements Serializable, Observer {

	// object containers
	private List<List<Element>> cells;
	private List<Agent> agents;
	// This holds all cells which are on fire or near fire (these are the only ones that need to be updated)
	private Set<Element> activeCells = new HashSet<>();
	// This holds all cells which can contain the fire, such as dirt and river cells
	private Set<Element> barriers = new HashSet<>();
	// This holds a list of previous states of the simulation if undo_redo==true, otherwise only the first state for reset
	private List<Simulation> states;

	// general parameters
	private int width;
	private int height;
	private int step_time;
	private int step_size;
	private int step_limit = 100;
	private boolean undo_redo;
	private boolean running;
	private boolean use_gui;
	private boolean generateRandom = false;
	private Random rand;
	private long randomizer_seed = 0;

	// parameters related to fitness
	int totalFuel = 0;
	int totalFuelBurnt = 0;

	// parameters related to wind
	private float wVecX;
	private float wVecY;
	private float windSpeed;

	// parameters related to agents
	private int nr_agents;
	private int energyAgents;
	private boolean useDijkstra = false;

	// other classes
	private ParameterManager parameter_manager;
	private Generator generator;
	private RLController rlController;

	public Simulation(boolean use_gui) {
		this.use_gui = use_gui;

		// Randomization initialization
		// Random seed_gen = new Random();
		// randomizer_seed = seed_gen.nextLong();
		rand = new Random(randomizer_seed);
		states = new ArrayList<>();

		// Initialize the parameters to some default values and make them available for drawing
		create_parameters();

		parameter_manager = new ParameterManager(this);
		parameter_manager.addObserver(this);
		generator = new Generator(this);

		// Generate a new map to start on
		if (generateRandom) {
			generator.randomMap();
		} else {
			parameter_manager.changeParameter("Model", "Width", 10f);
			parameter_manager.changeParameter("Model", "Height", 10f);
			generator.plainMap();
		}

		// Generate plan for agent(s)
		if (useDijkstra){
			setPathAgents();
		}

		findActiveCells();
		setChanged();
		notifyObservers(cells);
		notifyObservers(agents);

		// Save the state so it can be reset to
		states.add((Simulation) deepCopy(this));
	}

	/**
	 * Start a simulation if there exists a controller.
	 * Currently this does show a GUI with new MainFrame, but this can be removed for actual learning.
	 * @param controller
	 */
	public Simulation(RLController controller) {
		this(false);
		if(generateRandom){
			System.out.println("WARNING! GENERATING RANDOM MIGHT CAUSE NPE");
		}
		this.rlController = controller;
		for (Agent a: agents) {
			a.setController(rlController);
		}
		parameter_manager.changeParameter("Model", "Step Time", 0f);
	}


	/**
	 * This sets all tunable parameters to a default value, and adds it to the list of TextFields tuneable at runtime
	 * Due to HashMap restrictions it only works with Strings and Floats, so you should initialize a value with 3f.
	 * If you want to access the value of a parameter do parameters.get("Parameter name").floatValue()
	 */
	private void create_parameters() {
		width = 50;
		height = 50;
		nr_agents = 3;
		energyAgents = 20;
		if (use_gui) {
			step_time = 100;
		} else {
			step_time = 0;
		}
		step_size = 1;
		undo_redo = false;
		wVecX = -1;
		wVecY = 0;
		windSpeed = 0;
	}


	/**
	 * Start is linked to the start button. It moves one step forward every Step_time in ms.
	 * A negative time will make it perform steps backwards, but only if undo/redo is enabled.
	 * The loop will stop when running is set to false by calling stop() or pressing the stop button.
	 */
	public void start() {
		running = true;
		int nsteps = 0;
		while (running && nsteps < step_limit) {
			nsteps++;
			if (nsteps >= step_limit) {		// this makes it more clear when it's out of steps
				stop("step limit");
			}
			if (step_time >=0) {
				stepForward();
			} else {
				stepBack();
			}
			try {
				Thread.sleep(Math.abs((long) step_time));
			} catch (java.lang.InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * Pauses the simulation, linked to the stop button
	 */
	public void stop(String reason){
		running = false;
		//System.out.println("STOPPED: " + agents.size() + " agents left on " + activeCells.size() + " active cells " + "(" + reason + ")");
	}

	/**
	 * Resets the simulation to the first state since the last regeneration. Linked to the reset button.
	 */
	public void reset() {
		stop("reset");

		// Revert to the first state that was saved during generation
		if (states.size() > 0) {
			Simulation rewind = states.get(0);
			states.remove(0);
			this.cells = rewind.cells;
			this.agents = rewind.agents;
			this.activeCells = rewind.activeCells;
		}
		setChanged();
		notifyObservers(cells);
		notifyObservers(agents);
		findActiveCells();

		// Save the reset state again so we can reset the same map many times
		states.add((Simulation) deepCopy(this));
	}

	/**
	 * Clears all the cells and active cells and draws a new map.
	 * Currently this is the tree_grid since we don't have a map generation.
	 */
	public void regenerate() {
		stop("regenerate");
		states.clear();
		activeCells.clear();

		if (generateRandom) {
			generator.randomMap();
		} else {
			generator.plainMap();
		}
		for(Agent a : agents){
			a.setController(rlController);
		}
		if (useDijkstra){
			setPathAgents();
		}
		setChanged();
		notifyObservers(cells);
		notifyObservers(agents);
		findActiveCells();

		// Save the state (for reset)
		states.add((Simulation) deepCopy(this));
	}


	/**
	 *
	 * Revert the simulation by one time step if undo/redo is enabled.
	 * If there are no steps to take back anymore, the simulation is paused.
	 * Linked to both the Step back button, as well as running the simulation with a negative step time.
	 */
	public void stepBack() {
		if (undo_redo) {
			for (int i = 0; i< step_size; i++) {

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
	}

	/**
	 * Perform one step forward (and record the previous state if undo/redo is enabled).
	 * The step forward is performed by updateEnvironment(), and the new state is sent to the GUI with notifyObservers()
	 */
	public void stepForward() {
		for (int i = 0; i< step_size; i++) {
			if (undo_redo) {
				System.out.println("Adding undo_copy");
				states.add((Simulation) deepCopy(this));
			}
			updateEnvironment();
		}
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

	/**
	 *  Update the list of active cells. Apply the heat from the burning cell cell to all
	 *  of its neighbouring cells. If it ignites a neighbouring cell, add that cell to the
	 *  activeCells. If a burning cell runs out of fuel, remove it from the activeCells.
	 *
	 *  This function updates (non-agent) cells
	 */
	private void updateEnvironment() {
		// keep track of element to remove or add, we cant do that while iterating
		HashSet<Element> activesToRemove = new HashSet<>();
		HashSet<Element> activesToAdd = new HashSet<>();
		HashSet<Agent> agentsToRemove = new HashSet<>();

		if (agents.isEmpty() && activeCells.isEmpty()) {
			stop("empty sets");
		}

		for (Agent a : agents){
			String status = a.timeStep();
			if (status.equals("Dead")){
				agentsToRemove.add(a);
			}
		}
		agents.removeAll(agentsToRemove);

		// every burning cell will decrement its fuel level by one in each iteration
		totalFuelBurnt += activeCells.size();

		// for each burning cell
		for (Element burningCell : activeCells) {
			// update the cell and remove if it is burnt out
			String status = burningCell.timeStep();
			if (status.equals("Dead")) {
				activesToRemove.add(burningCell);
			}
			// if it is still burning, apply heat to neighbouring cells
			if (status.equals("No Change")) {
				HashSet<Element> neighbours = burningCell.getNeighbours(cells, agents);
				for (Element neighbourCell : neighbours) {
					if (neighbourCell.isBurnable()) {
						status = neighbourCell.getHeatFrom(burningCell);
						// if it ignited, add it to activeCells
						if (status.equals("Ignited")) {
							activesToAdd.add(neighbourCell);
						}
					}
				}
			}

		}
		activeCells.removeAll(activesToRemove);
		activeCells.addAll(activesToAdd);
	}

	/**
	 *  Initializes activeCells by searching the entire map for burning cells
	 * 	and adding those and their neighbours
	 * 	Also adds the agents to activeCells
	 */
	private void findActiveCells()
	{
		activeCells = new HashSet<>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Element cell = cells.get(x).get(y);
				if (cell.isBurning()) {
					activeCells.add(cell);
				}
			}
		}
	}


	/**
	 * Return the parameters currently set, to be used by the parameter manager.
	 * The values are defined in createParameters()
	 * @return
	 */
	public Map<String, Float> getParameters() {
		Map<String, Float> return_map = new HashMap<>();
		return_map.put("Width", (float) width);
		return_map.put("Height", (float) height);
		return_map.put("Number of Agents", (float) nr_agents);
		return_map.put("Energy of Agents", (float) energyAgents);
		return_map.put("Step Size", (float) step_size);
		return_map.put("Step Time", (float) step_time);
		return_map.put("Undo/Redo", undo_redo ? 1f : 0f);
		return_map.put("Wind x", wVecX);
		return_map.put("Wind y", wVecY);
		return_map.put("Wind Speed", windSpeed);
		return return_map;
	}


	/**
	 * This makes a full copy of any Serializable object, including it's children.
	 * This is needed for being able to revert to previous states and circumventing Java's pass-by-reference
	 * It's probably best to just leave this code as is unless you understand what is going on here.
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
		// IF a Map.Entry<String, Map.Entry<String, Float>> and the first string is "Model"
		if(o instanceof Map.Entry
				&& ((Map.Entry) o).getKey() instanceof String
				&& ((Map.Entry) o).getValue() instanceof Map.Entry
				&& ((Map.Entry) o).getKey() == "Model") {
			Float value = (Float) ((Map.Entry) ((Map.Entry) o).getValue()).getValue();
			switch( (String) ((Map.Entry) ((Map.Entry) o).getValue()).getKey() ) {
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
				case "Number of Agents":
					nr_agents = value.intValue();
					break;
				case "Wind x":
					wVecX = value;
					break;
				case "Wind y":
					wVecY = value;
					break;
				case "Wind Speed":
					windSpeed = value;
					break;
				case "Energy of Agents":
					energyAgents = value.intValue();
				default:
					System.out.println("No action defined in Simulation.update for " +
							(String) ((Map.Entry) ((Map.Entry) o).getValue()).getKey());
			}
		}
	}

	/**
	 * Needed to give controlPanel access to parameterManager
	 * @return
	 */
	public ParameterManager getParameter_manager() { return parameter_manager; }

	public Random getRand() { return rand; }

	public void setRand(Random rand) { this.rand = rand; }

	public int getRandX() { return rand.nextInt(width); }

	public int getRandY() { return rand.nextInt(height); }

	public List<Agent> getAgents() { return agents; }

	public int getNr_agents() { return nr_agents; }

	public void setNr_agents(int nr_agents) { this.nr_agents = nr_agents; }

	public void setCells(List<List<Element>> cells){ this.cells = cells; }

	public void setAgents(List<Agent> agents) { this.agents = agents; }

	public int getEnergyAgents() { return energyAgents; }

	public Set<Element> getActiveCells() { return activeCells; }

	public Set<Element> getBarriers() { return barriers; }

	public void addToBarriers(Element b) {
		barriers.add(b);
	}

	public int getTotalFuel() { return totalFuel; }

	public int getTotalFuelBurnt() { return totalFuelBurnt; }

	public boolean isInBounds(int x, int y) {
		return (   x >= 0 && x < width
				&& y >= 0 && y < height);
	}

	public Element getElementAt(int x, int y) {
		return cells.get(x).get(y);
	}

	public Element getNearestFireTo(int x, int y) {
		Element origin = cells.get(x).get(y);
		Element nearest = null;
		double minDistance = 10000;
		for (Element f : activeCells) {
			if (origin.distanceTo(f) < minDistance) {
				nearest = f;
			}
		}
		return nearest;
	}

	public void applyUpdates(){
		setChanged();
		notifyObservers(cells);
	}

	/**
	 * For now, the sole purpose of this function is to provide some path finding functionality. Once other there is a
	 * proper use for pathfinding, this function is redundant and can be removed.
	 */
	public void setPathAgents(){
		for (Agent a: agents){
			DijkstraShortestPath sp = new DijkstraShortestPath(cells,a,cells.get(49).get(49));
			sp.findPath();
			a.setPath(sp);
			//a.setPlan(sp.getDirections());
		}
	}
}
