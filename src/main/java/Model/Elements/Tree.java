package Model.Elements;

import Model.ParameterManager;

import java.awt.Color;
import java.util.Map;

public class Tree extends Element
{
	private static final Color DARK_GREEN = new Color(0,102,0);
	public Tree(int x, int y, ParameterManager parameterManager)
	{
		this.x = x;
		this.y = y;
		this.parameterManager = parameterManager;
		initializeParameters();
		pullParameters();
	}

	public void initializeParameters()
	{
		this.type = "Tree";
		this.r = 2;
		this.burnable = true;
		this.color = DARK_GREEN;
		this.burnIntensity = 5;
		this.ignitionThreshold = 1;
		this.fuel = 50;
		this.moveSpeed = 1;
	}
}
