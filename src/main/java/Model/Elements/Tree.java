package Model.Elements;

import java.awt.Color;
import java.util.Map;

public class Tree extends Element
{
	public Tree(int x, int y, Map<String, Float> parameters)
	{
		this.x = x;
		this.y = y;
		this.parameters = parameters;
		initializeParameters();
	}

	public void initializeParameters()
	{
		this.r = 2;
		this.burnable = true;
		this.color = Color.GREEN;
		this.burnIntensity = 5;
		this.ignitionThreshold = 1;
		this.fuel = 50;
		this.moveSpeed = 1;
	}
}
