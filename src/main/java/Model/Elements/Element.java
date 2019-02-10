package Model.Elements;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/*
	The abstract element class contains most of what is needed for each element,
	each subclass must implement initializeParameters() which defines the unique
	properties of each specific element.
 */

public abstract class Element
{
	int x = 0;
	int y = 0;
	int r = 0;
	Color color = Color.WHITE;
	Map<String, Float> parameters;
	boolean burnable = false;
	boolean isBurning = false;
	boolean isBurnt = false;

	int fireActivity = 0;
	int burnIntensity = 0;
	int ignitionThreshold = 10;
	int fuel = 0;

	public abstract void initializeParameters();

	/*
		Updates the cell and the fire activity of its neighbours, returns a
		simple status string to help keep track of active cells
	 */
	public String update(List<List<Element>> cells)
	{
		if (!burnable)
		{
			return "Not Burnable";
		}
		boolean wasBurning = isBurning;
		timeStep();
		if (!isBurning)
		{
			updateFireActivity(cells, "remove");
			return "Dead";
		}
		updateFireActivity(cells, "add");
		if (!wasBurning)
		{
			return "Ignited";
		}
		return "No Change";
	}

	/*
		At every time step, the fuel of the cell is reduced by its burnIntensity if the
		cell is burning, and it is burnt out of there is no more fuel left. If the cell
		is not burning, we check if the ignitionThreshold is reached and possibly ignite.
	 */
	private void timeStep()
	{
		if (isBurning)
		{
			fuel -= burnIntensity;
			if (fuel <= 0)
			{
				isBurning = false;
				isBurnt = true;
			}
		}
		else
		{
			if (fireActivity > ignitionThreshold)
			{
				isBurning = true;
			}
		}
	}

	/*
		Currently implemented as cumulative: Every time this is called, the cells within
		range (of circle provided by (x, y) and r) of this cell get the burnIntensity of
		this cell added to their fireActivity.
	 */
	private void updateFireActivity(List<List<Element>> cells, String command)
	{
		HashSet<Element> neighbours = getNeighbours(cells);
		for (Element cell : neighbours)
		{
			if (command.equals("add"))
			{
				cell.fireActivity += this.burnIntensity;
			}
			if (command.equals("remove"))
			{
				cell.fireActivity -= 3 * this.burnIntensity;
			}
		}
	}

	/*
		Returns a set of cells that fall within the range of this cell.
	 */
	public HashSet<Element> getNeighbours(List<List<Element>> cells)
	{
		HashSet<Element> neighbours = new HashSet<>();
		for (int xi = x-r; xi < x+r; xi++)
		{
			for (int yi = y-r; yi < y+r; yi++)
			{
				if (inBounds(xi, yi))
				{
					Element cell = cells.get(xi).get(yi);
					if (cell.isWithinCircleOf(this))
					{
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
	private boolean isWithinCircleOf(Element cell)
	{
		return Math.pow(x - cell.x, 2) + Math.pow(y - cell.y, 2) <= Math.pow(r, 2);
	}

	/*
		Checks if the coordinates are within the boundaries of the map.
	 */
	private boolean inBounds(int x, int y)
	{
		int maxX = parameters.get("Height").intValue();
		int maxY = parameters.get("Width").intValue();
		if (x >= 0 && x < maxX)
		{
			if (y >= 0 && y < maxY)
			{
				return true;
			}
		}
		return false;
	}

	public void setBurning() { isBurning = true; }

	public boolean isBurning() { return isBurning; }

	public boolean isBurnt() { return isBurnt; }

	public boolean isBurnable() { return burnable; }

	public int getX() { return x; }

	public int getY() { return y; }

	public Color getColor() { if (isBurning) { return Color.RED; } else { return color; } }

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
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

}
