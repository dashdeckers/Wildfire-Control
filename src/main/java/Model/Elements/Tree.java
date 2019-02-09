package Model.Elements;

import java.awt.Color;

public class Tree extends Element
{
	public Tree(int x, int y)
	{
		this.x = x;
		this.y = y;
		initializeParameters();
	}

	public void initializeParameters()
	{
		this.burnable = true;
		this.color = Color.GREEN;
		this.burnDuration = 3;
		this.burnTimer = burnDuration;
		this.burnIntensity = 3;
		this.ignitionThreshold = 3;
		this.fireActivity = 0;
		this.isBurning = false;
		this.isBurnt = false;
	}
}
