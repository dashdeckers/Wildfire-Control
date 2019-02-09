package Model;

import Model.Elements.Element;
import Model.Elements.Tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
	The activeCells set contains java tuples which store the coordinates (x, y) or (row, col)
	of active cells. Active cells are cells which are nearby, or are themselves a burning cell.
	When the simulation runs, neighbours of cells that just started burning are added to the
	activeCells, while cells that are burnt out are removed.
 */

public class Simulation
{
    private List<List<Element>> cells;
    private Set<Tuple> activeCells;

    private boolean isRunning = false;
	private int size;
	private int neighbourRadius = 1;

    public Simulation(int size)
	{
		this.size = size;
        tree_grid(size, size);
        findActiveElements();
        System.out.println(activeCells);
    }

    // Runs the simulation
    public void start()
    {
    	int counter = 0;

    	// loops forever.. gotta fix that
    	isRunning = true;
    	while (isRunning)
        {
        	counter++;
        	if (counter > 100)
			{
				break;
			}
        	// if there are no active cells, stop running
        	if (activeCells.isEmpty())
			{
				isRunning = false;
			}
        	update();
        }
    }

    public void stop()
    {
        this.isRunning = false;
    }

    public void reset(){

    }

    public List<List<Element>> getAllCells() {
        return cells;
    }

    public void update()
	{
		// remember elements to add to- or remove from set because we can't while iterating
		HashSet toRemove = new HashSet<Tuple>();
		HashSet toAdd = new HashSet<Tuple>();
		// for each active cell (= burning, or near burning)
		for (Tuple cell : activeCells)
		{
			// get the actual cell using the reference coordinates
			int row = cell.x;
			int col = cell.y;
			Element c = cells.get(row).get(col);
			// if it is no longer active, remove it from activeCells
			if (c.isBurnt())
			{
				toRemove.add(cell);
			}
			// if it is still active
			else
			{
				// update it, but check if it just started burning
				boolean wasNotBurning = false;
				boolean isBurningNow = false;

				if (!c.isBurning())
				{
					wasNotBurning = true;
				}
				c.update();
				if (c.isBurning())
				{
					isBurningNow = true;
				}
				// if it just started burning, add neighbouring cells to activeCells
				if (wasNotBurning && isBurningNow)
				{
					toAdd.addAll(getNeighbours(c.getX(), c.getY()));
				}
			}
		}
		activeCells.addAll(toAdd);
		activeCells.removeAll(toRemove);
	}

    // This initializes the activeCells field and adds to it every burning cell, as well as
	// every cell which is within the neighbourRadius of a burning cell. It parses the entire
	// map, duplicates are rejected by the set.
    private void findActiveElements()
	{
		this.activeCells = new HashSet<>();
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				if (cells.get(x).get(y).isBurning())
				{
					activeCells.addAll(getNeighbours(x, y));
				}
			}
		}
	}

	// Returns a set containing (the coordinates of) the neighbours of (cx, cy)
	public HashSet<Tuple> getNeighbours(int cx, int cy)
	{
		HashSet<Tuple> neighbours = new HashSet<>();
		for (int x = cx-neighbourRadius; x < cx+neighbourRadius; x++)
		{
			for (int y = cy-neighbourRadius; y < cy+neighbourRadius; y++)
			{
				neighbours.add(new Tuple(x, y));
			}
		}
		return neighbours;
	}

	public void addToActiveElements(HashSet<Tuple> set)
	{
		activeCells.addAll(set);
	}


    //Dummy function creating only tree tiles for testing GUI
    public void tree_grid(int x, int y){
        cells = new ArrayList<List<Element>>();
        for(int i = 0; i<x; i++){
            List<Element> row = new ArrayList<Element>();
            for(int j=0; j<y; j++){
                if(i== 5 && j == 5){
                    Element t = new Tree(i,j);
                    t.setBurning();
                    row.add(t);
                }else {
                    row.add(new Tree(i, j));
                }
            }
            cells.add(row);
        }
    }

	public class Tuple
	{
		int x;
		int y;

		Tuple(int i, int j)
		{
			this.x = i;
			this.y = j;
		}
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
			Tuple other = (Tuple) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
	}
}
