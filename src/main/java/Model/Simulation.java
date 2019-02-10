package Model;

import Model.Elements.Element;
import Model.Elements.Tree;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Observable;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class Simulation extends Observable implements Serializable
{
	private List<List<Element>> cells;
	private Set<Element> activeCells = new HashSet<>();
	private List<Simulation> states;
	private Map<String, Float> parameters;
	private Map<String, Float> staged_parameters;
	private boolean running;

	private Random rand;

	public Simulation()
	{
	    //Initialize these things
        parameters = new HashMap<>();
        staged_parameters = new HashMap<>();
        rand = new Random();
        states = new ArrayList<>();


        create_parameters();

        tree_grid(parameters.get("Width").intValue(), parameters.get("Height").intValue());

		findActiveCells();
		printActiveCells();

		this.updateEnvironment();
		printActiveCells();

		cells.get(0).get(0).setBurning();
		printActiveCells();
	}

	private void printActiveCells()
	{
		System.out.println("Number of active Cells: " + activeCells.size());
		String output = "";
		for (Element cell : activeCells)
		{
			output += "(" + cell.getX() + " " + cell.getY() + ") ";
		}
		System.out.println(output);
	}

    public void start() {
	    running = true;
        while(running){
            try {
                Thread.sleep(Math.abs((long) parameters.get("Step time").floatValue()));
            } catch (java.lang.InterruptedException e){
                System.out.println(e.getMessage());
            }
            if(parameters.get("Step time") >=0){
                stepForward();
            }else{
                stepBack();
            }

        }
    }
    public void stop(){
        running = false;
    }
    public void reset(){
	    if(states.size() > 0){
	        Simulation rewind = states.get(0);
	        states.clear();
	        this.cells =rewind.cells;
	        this.activeCells = rewind.activeCells;
            setChanged();
            notifyObservers(cells);
        }
    }
    public void regenerate() {
        for (String s : staged_parameters.keySet()) {
            parameters.put(s, staged_parameters.get(s));
        }
        states.clear();
        activeCells.clear();
        tree_grid(parameters.get("Width").intValue(), parameters.get("Height").intValue());
        findActiveCells();
    }
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

    public void stepForward(){
	    if(parameters.get("Undo/redo").intValue() == 1) {
            states.add((Simulation) deepCopy(this));
        }
        updateEnvironment();
        setChanged();
        notifyObservers(cells);

    }

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
		System.out.println("Updating");

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
	}

	/*
		Initializes activeCells by searching the entire map for burning cells
		and adding those and their neighbours
	 */
    private void findActiveCells()
	{
		for (int x = 0; x < parameters.get("Height").intValue(); x++)
		{
			for (int y = 0; y < parameters.get("Width").intValue(); y++)
			{
				Element cell = cells.get(x).get(y);
				if (cell.isBurning())
				{
					activeCells.addAll(cell.getNeighbours(cells));
				}
			}
		}
	}

    //Dummy function creating only tree tiles for testing GUI
	private void tree_grid(int x, int y){
        int fire_x = rand.nextInt(x);
        int fire_y = rand.nextInt(y);
        cells = new ArrayList<List<Element>>();
        for(int i = 0; i<x; i++){
            List<Element> row = new ArrayList<Element>();
            for(int j=0; j<y; j++){
                if(i== fire_x && j == fire_y){
                    System.out.println("Fire at " + i + "," + j);
                    Element t = new Tree(i,j, parameters);
                    t.setBurning();
                    row.add(t);
                }else {
                    row.add(new Tree(i, j, parameters));
                }
            }
            cells.add(row);
        }
        setChanged();
        notifyObservers(cells);
    }

    public void create_parameters(){
        //Reverse order of the way they are drawn
        parameters.put("Undo/redo", 0f);
        parameters.put("Fire speed", 1f);
        parameters.put("Wind strength", 1f);
        parameters.put("Step time", 100f);
        parameters.put("Step size", 1f);
        parameters.put("Width", 20f);
        parameters.put("Height", 20f);
    }

    public void changeParameter(String s, float v){
        switch (s){
            case "Height":
            case "Width" :
                staged_parameters.put(s,v);
                break;
            default:
                parameters.put(s, v);
        }

    }

    public Map<String, Float> getParameters(){
        return parameters;
    }


    /**
     * Makes a deep copy of any Java object that is passed.
     * Not a clue how this works though.
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
